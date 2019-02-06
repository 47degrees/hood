package com.adrianrafo.hood

import arrow.core.Option
import arrow.core.toOption
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.monad.monad
import arrow.instances.list.foldable.nonEmpty
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class CompareBenchmarkCI : DefaultTask() {

  @get:Input
  var previousBenchmarkPath: String = project.objects.property<String>().getOrElse("master.csv")
  @get:Input
  var currentBenchmarkPath: String = project.objects.property<String>().getOrElse("current.csv")
  @get:Input
  var keyColumnName: String = project.objects.property<String>().getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property<String>().getOrElse("Score")
  @get:Input
  var threshold: Int = project.objects.property<Int>().getOrElse(50)
  @get:Input
  var token: Option<String> = project.objects.property<String>().orNull.toOption()

  private val ciName: String = "travis"

  private fun getWrongResults(result: List<BenchmarkResult>): List<BenchmarkResult> =
    result.filter { it::class == BenchmarkResult.ERROR::class || it::class == BenchmarkResult.FAILED::class }

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
        token.fold({ IO.raiseError(GradleException("Error getting Github token")) }) { IO { it } }
          .bind()

      val info = GhInfo(owner, repo, token)
      val commitSha: String = IO { System.getenv("TRAVIS_PULL_REQUEST_SHA") }.bind()

      GithubIntegration.setStatus(
        info,
        commitSha,
        GhStatus(GhStatusState.Pending, "Comparing Benchmarks")
      ).bind()

      val result: List<BenchmarkResult> = Comparator.compareCsv(
        previousBenchmarkPath,
        currentBenchmarkPath,
        threshold,
        keyColumnName,
        compareColumnName
      ).bind()

      val previousComment = GithubIntegration.getPreviousCommentId(info, ciName, pr.toInt()).bind()
      val cleanResult =
        previousComment.fold({ IO { true } }) { GithubIntegration.deleteComment(info, it) }.bind()

      GithubIntegration.createComment(info, pr.toInt(), result)
        .flatMap { if (it && cleanResult) IO.unit else GithubIntegration.raiseError("Error creating the comment") }
        .bind()

      val errors: List<BenchmarkResult> = getWrongResults(result)

      if (errors.nonEmpty()) {
        GithubIntegration.setStatus(
          info,
          commitSha,
          GhStatus(GhStatusState.Failed, "Benchmarks comparison failed")
        ).bind()
        IO.raiseError<Unit>(GradleException(errors.prettyPrintResult())).bind()
      } else
        GithubIntegration.setStatus(
          info,
          commitSha,
          GhStatus(GhStatusState.Succeed, "Benchmarks comparison passed")
        ).bind()

    } else IO.unit.bind()

  }.fix().unsafeRunSync()

}
