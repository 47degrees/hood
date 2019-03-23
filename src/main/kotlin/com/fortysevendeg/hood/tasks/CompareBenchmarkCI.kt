package com.fortysevendeg.hood.tasks

import arrow.core.toOption
import arrow.data.extensions.list.foldable.nonEmpty
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import arrow.effects.handleErrorWith
import com.fortysevendeg.hood.*
import com.fortysevendeg.hood.github.GithubCommentIntegration
import com.fortysevendeg.hood.github.GithubCommon
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
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
  var threshold: Double? = project.objects.property(Double::class.java).orNull
  @get:Input
  var token: String? = project.objects.property(String::class.java).orNull

  private fun getWrongResults(result: List<BenchmarkComparison>): List<BenchmarkComparison> =
    result.filter { it.result::class == BenchmarkResult.ERROR::class || it.result::class == BenchmarkResult.FAILED::class }

  private fun compareCI(info: GhInfo, commitSha: String, pr: Int) = fx {

    !GithubCommentIntegration.setPendingStatus(
      info,
      commitSha
    )

    val result: List<BenchmarkComparison> = !Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      keyColumnName,
      compareColumnName,
      thresholdColumnName,
      threshold.toOption()
    )

    val previousComment = !GithubCommentIntegration.getPreviousCommentId(info, pr)

    val (commentResult) = previousComment.fold({
      GithubCommentIntegration.createComment(info, pr, result)
    }) { GithubCommentIntegration.updateComment(info, it, result) }

    if (commentResult) !IO.unit
    else !GithubCommon.raiseError("Error creating the comment")

    !OutputFile.sendOutputToFile(outputToFile, outputPath, result, outputFormat)

    val errors: List<BenchmarkComparison> = getWrongResults(result)

    if (errors.nonEmpty()) {
      !GithubCommentIntegration.setFailedStatus(
        info,
        commitSha,
        errors.joinToString { it.key }
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
  fun compareBenchmarkCI(): Unit =
    IO { System.getenv("TRAVIS_PULL_REQUEST") }.flatMap {

      if (!it.contains("false")) {
        fx {

          val slug = !IO { System.getenv("TRAVIS_REPO_SLUG").split('/') }
          if (slug.size < 2) !raiseError<Unit>(GradleException("Error reading env var: TRAVIS_REPO_SLUG"))
          val owner: String = slug.first()
          val repo: String = slug.last()

          val (token: String) =
            token.toOption()
              .fold({ raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }

          val info = GhInfo(owner, repo, token)
          val commitSha: String = !IO { System.getenv("TRAVIS_PULL_REQUEST_SHA") }

          !compareCI(info, commitSha, it.toInt())
        }.fix()
      } else IO.unit

    }.unsafeRunSync()

}
