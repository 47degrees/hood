package com.adrianrafo.hood

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

  fun getWrongResults(result: List<BenchmarkResult>): List<BenchmarkResult> =
    result.filter { it::class == BenchmarkResult.ERROR::class || it::class == BenchmarkResult.FAILED::class }

  @Suppress("UNREACHABLE_CODE")
  @TaskAction
  fun compareBenchmarkCI() = IO.monad().binding {
    val info: GhInfo = TODO()
    val commitSha : String = TODO()

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

    println(result.prettyPrintResult())
    GithubIntegration.setCommentResult(info, result).bind()
    val errors = getWrongResults(result)
    if (errors.nonEmpty()) {
      GithubIntegration.setStatus(info, commitSha, GhStatus(GhStatusState.Failed, "Benchmarks comparison failed")).bind()
      IO.raiseError(throw GradleException(errors.prettyPrintResult())).bind()
    } else
      GithubIntegration.setStatus(info, commitSha, GhStatus(GhStatusState.Succeed, "Benchmarks comparison passed")).bind()
  }.fix().unsafeRunSync()

}
