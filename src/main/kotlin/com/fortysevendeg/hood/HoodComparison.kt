package com.fortysevendeg.hood

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.toOption
import arrow.data.extensions.list.foldable.nonEmpty
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import arrow.effects.handleErrorWith
import com.fortysevendeg.hood.github.GhInfo
import com.fortysevendeg.hood.github.GithubCommentIntegration
import com.fortysevendeg.hood.github.GithubCommon
import com.fortysevendeg.hood.models.*
import java.io.File
import java.net.URI

object HoodComparison {
  fun compare(
    previousBenchmarkPath: File,
    currentBenchmarkPath: List<File>,
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    outputToFile: Boolean,
    outputPath: String,
    outputFormat: String,
    generalThreshold: Double?,
    benchmarkThreshold: Map<String, Double>?,
    include: String?,
    exclude: String?
  ): IO<Unit> = fx {

    val resultOrError = !Comparator.compareBenchmarks(
      previousBenchmarkPath,
      currentBenchmarkPath,
      keyColumnName,
      compareColumnName,
      thresholdColumnName,
      generalThreshold.toOption(),
      benchmarkThreshold.toOption(),
      include.toOption().map(String::toRegex),
      exclude.toOption().map(String::toRegex)
    )

    println(resultOrError.prettyOutputResult())

    val result = !resultOrError
      .flatMap(List<BenchmarkComparison>::handleFailures)
      .fromEither(BenchmarkComparisonError::error)

    val allJson = JsonSupport.areAllJson(currentBenchmarkPath.plus(previousBenchmarkPath))

    !OutputFile.sendOutputToFile(outputToFile, allJson, outputPath, result, outputFormat)

  }

  fun compareCI(
    previousBenchmarkPath: File,
    currentBenchmarkPath: List<File>,
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    outputToFile: Boolean,
    outputPath: String,
    outputFormat: String,
    generalThreshold: Double?,
    benchmarkThreshold: Map<String, Double>?,
    include: String?,
    exclude: String?,
    info: GhInfo,
    commitSha: String,
    pr: Int,
    statusTargetUrl: Option<URI>
  ): IO<Unit> = fx {

    !GithubCommentIntegration.setPendingStatus(
      info,
      commitSha,
      statusTargetUrl
    )

    val resultOrError: Either<BenchmarkComparisonError, List<BenchmarkComparison>> =
      !Comparator.compareBenchmarks(
        previousBenchmarkPath,
        currentBenchmarkPath,
        keyColumnName,
        compareColumnName,
        thresholdColumnName,
        generalThreshold.toOption(),
        benchmarkThreshold.toOption(),
        include.toOption().map(String::toRegex),
        exclude.toOption().map(String::toRegex)
      )

    val result = !resultOrError.getOrRaiseError(BenchmarkComparisonError::error)

    val previousComment = !GithubCommentIntegration.getPreviousCommentId(info, pr)

    val (commentResult) = previousComment.fold({
      GithubCommentIntegration.createComment(info, pr, result)
    }) { GithubCommentIntegration.updateComment(info, it, result) }

    if (commentResult) !IO.unit
    else !GithubCommon.raiseError("Error creating the comment")

    val allJson = JsonSupport.areAllJson(currentBenchmarkPath.plus(previousBenchmarkPath))

    !OutputFile.sendOutputToFile(outputToFile, allJson, outputPath, result, outputFormat)

    val errors: List<BenchmarkComparison> = result.getWrongResults()

    if (errors.nonEmpty()) {
      !GithubCommentIntegration.setFailedStatus(
        info,
        commitSha,
        errors.joinToString(transform = BenchmarkComparison::key),
        statusTargetUrl
      ).flatMap { IO.raiseError<Unit>(BadPerformanceBenchmarkError(errors)) }
    } else
      !GithubCommentIntegration.setSuccessStatus(
        info,
        commitSha,
        statusTargetUrl
      )

  }.fix().handleErrorWith { ex ->
    if (ex is BenchmarkError) GithubCommentIntegration.setFailedStatus(
      info,
      commitSha,
      ex.localizedMessage,
      statusTargetUrl
    ).flatMap {
      @Suppress("RemoveExplicitTypeArguments")
      IO.raiseError<Unit>(ex)
    } else {
      println("Unexpected error during CI comparison, moving to standard comparison: ${ex.localizedMessage}")
      compare(
        previousBenchmarkPath,
        currentBenchmarkPath,
        keyColumnName,
        compareColumnName,
        thresholdColumnName,
        outputToFile,
        outputPath,
        outputFormat,
        generalThreshold,
        benchmarkThreshold,
        include,
        exclude
      )
    }
  }

}