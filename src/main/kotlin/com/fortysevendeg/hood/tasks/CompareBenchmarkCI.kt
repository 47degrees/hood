package com.fortysevendeg.hood.tasks

import arrow.core.Either
import arrow.core.extensions.option.applicative.map2
import arrow.core.toOption
import arrow.data.extensions.list.foldable.nonEmpty
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import arrow.effects.handleErrorWith
import com.fortysevendeg.hood.Comparator
import com.fortysevendeg.hood.JsonSupport
import com.fortysevendeg.hood.OutputFile
import com.fortysevendeg.hood.github.GhInfo
import com.fortysevendeg.hood.github.GithubCommentIntegration
import com.fortysevendeg.hood.github.GithubCommon
import com.fortysevendeg.hood.models.BenchmarkComparison
import com.fortysevendeg.hood.models.BenchmarkComparisonError
import com.fortysevendeg.hood.models.BenchmarkResult
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*
import java.io.File

open class CompareBenchmarkCI : DefaultTask() {

  //Benchmarks paths
  @get:InputFile
  var previousBenchmarkPath: File =
    project.objects.fileProperty().asFile.getOrElse(File("master.csv"))
  @get:InputFiles
  var currentBenchmarkPath: List<File> =
    project.objects.listProperty(File::class.java).getOrElse(emptyList())

  //Field names
  @get:Input
  var keyColumnName: String = project.objects.property(String::class.java).getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property(String::class.java).getOrElse("Score")
  @get:Input
  var thresholdColumnName: String =
    project.objects.property(String::class.java).getOrElse("Score Error (99.9%)")

  //Output
  @get:Input
  var outputToFile: Boolean = project.objects.property(Boolean::class.java).getOrElse(false)
  @get:Input
  var outputPath: String =
    project.objects.property(String::class.java).getOrElse("./hood/comparison")
  @get:Input
  var outputFormat: String =
    project.objects.property(String::class.java).getOrElse("md")

  //Extra
  @get:Input
  @Optional
  var generalThreshold: Double? = project.objects.property(Double::class.java).orNull
  @get:Input
  @Optional
  var benchmarkThreshold: Map<String, Double>? =
    project.objects.mapProperty(String::class.java, Double::class.java).orNull
  @get:Input
  @Optional
  var include: String? = project.objects.property(String::class.java).orNull
  @get:Input
  @Optional
  var exclude: String? = project.objects.property(String::class.java).orNull

  //CI
  @get:Input
  var token: String? = project.objects.property(String::class.java).orNull
  @get:Input
  var slug: String? = project.objects.property(String::class.java).orNull
  @get:Input
  var pullRequestSha: String? = project.objects.property(String::class.java).orNull
  @get:Input
  var pullRequestNumber: Int = project.objects.property(Int::class.java).getOrElse(-1)

  private fun getWrongResults(result: List<BenchmarkComparison>): List<BenchmarkComparison> =
    result.filter { it.result::class == BenchmarkResult.FAILED::class }

  private fun compareCI(info: GhInfo, commitSha: String, pr: Int) = fx {

    !GithubCommentIntegration.setPendingStatus(
      info,
      commitSha
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

    val errors: List<BenchmarkComparison> = getWrongResults(result)

    if (errors.nonEmpty()) {
      !GithubCommentIntegration.setFailedStatus(
        info,
        commitSha,
        errors.joinToString(transform = BenchmarkComparison::key)
      )
    } else
      !GithubCommentIntegration.setSuccessStatus(
        info,
        commitSha
      )

  }.fix().handleErrorWith {
    GithubCommentIntegration.setFailedStatus(
      info,
      commitSha,
      it.localizedMessage
    )
  }

  @TaskAction
  fun compareBenchmarkCI() {
    val maybeSlug = slug.toOption()
    val maybeSha = pullRequestSha.toOption()
    maybeSlug.map2(maybeSha) { (slug, sha) ->
      (if (pullRequestNumber != -1 && slug.contains('/') ) {
        fx {
          val separatedSlug = !IO { slug.split('/') }
          if (separatedSlug.size < 2) !raiseError<Unit>(GradleException("Invalid slug format"))
          val owner: String = separatedSlug.first()
          val repo: String = separatedSlug.last()

          val (token: String) =
            token.toOption()
              .fold({ raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }

          val info = GhInfo(owner, repo, token)

          !compareCI(info, sha, pullRequestNumber)
        }.fix()
      } else IO.unit).unsafeRunSync()
    }
  }

}
