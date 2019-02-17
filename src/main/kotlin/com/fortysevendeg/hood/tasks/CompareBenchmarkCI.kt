package com.fortysevendeg.hood.tasks

import arrow.core.toOption
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.monad.monad
import arrow.instances.list.foldable.nonEmpty
import com.fortysevendeg.hood.*
import com.fortysevendeg.hood.github.GithubCommentIntegration
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CompareBenchmarkCI : DefaultTask() {

  @get:InputFile
  var previousBenchmarkPath: File =
    project.objects.property(File::class.java).getOrElse(File("master.csv"))
  @get:InputFiles
  var currentBenchmarkPath: List<File> =
    project.objects.listProperty(File::class.java).getOrElse(emptyList())
  @get:Input
  var keyColumnName: String = project.objects.property(String::class.java).getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property(String::class.java).getOrElse("Score")
  @get:Input
  var threshold: Int = project.objects.property(Int::class.java).getOrElse(50)
  @get:Input
  var token: String? = project.objects.property(String::class.java).orNull

  private val ciName: String = "travis"

  private fun getWrongResults(result: List<BenchmarkComparison>): List<BenchmarkComparison> =
    result.filter { it.result::class == BenchmarkResult.ERROR::class || it.result::class == BenchmarkResult.FAILED::class }

  @TaskAction
  fun compareBenchmarkCI(): Unit = IO.monad().binding {
    val pr: String = IO { System.getenv("TRAVIS_PULL_REQUEST") }.bind()

    if (!pr.contains("false")) {

      val slug = IO { System.getenv("TRAVIS_REPO_SLUG").split('/') }.bind()
      if (slug.size < 2)
        IO.raiseError<Unit>(GradleException("Error reading env var: TRAVIS_REPO_SLUG")).bind()
      val owner: String = slug.first()
      val repo: String = slug.last()

      val token: String =
        token.toOption()
          .fold({ IO.raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }
          .bind()

      val info = GhInfo(owner, repo, token)
      val commitSha: String = IO { System.getenv("TRAVIS_PULL_REQUEST_SHA") }.bind()

      GithubCommentIntegration.setStatus(
        info,
        commitSha,
        GhStatus(
          GhStatusState.Pending,
          "Comparing Benchmarks"
        )
      ).bind()

      val result: List<BenchmarkComparison> = Comparator.compareCsv(
        previousBenchmarkPath,
        currentBenchmarkPath,
        threshold,
        keyColumnName,
        compareColumnName
      ).bind()

      val previousComment =
        GithubCommentIntegration.getPreviousCommentId(info, ciName, pr.toInt()).bind()
      val cleanResult =
        previousComment.fold({ IO { true } }) { GithubCommentIntegration.deleteComment(info, it) }
          .bind()

      GithubCommentIntegration.createComment(info, pr.toInt(), result)
        .flatMap { if (it && cleanResult) IO.unit else GithubCommentIntegration.raiseError("Error creating the comment") }
        .bind()

      val errors: List<BenchmarkComparison> = getWrongResults(result)

      if (errors.nonEmpty()) {
        GithubCommentIntegration.setStatus(
          info,
          commitSha,
          GhStatus(
            GhStatusState.Failed,
            "Benchmarks comparison failed"
          )
        ).bind()
        IO.raiseError<Unit>(GradleException(errors.prettyPrintResult())).bind()
      } else
        GithubCommentIntegration.setStatus(
          info,
          commitSha,
          GhStatus(
            GhStatusState.Succeed,
            "Benchmarks comparison passed"
          )
        ).bind()

    } else IO.unit.bind()

  }.fix().unsafeRunSync()

}
