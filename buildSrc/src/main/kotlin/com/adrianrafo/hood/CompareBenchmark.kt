package com.adrianrafo.hood

import arrow.instances.list.foldable.nonEmpty
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class CompareBenchmark : DefaultTask() {

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

  @TaskAction
  fun compareBenchmark() {
    val result: List<BenchmarkResult> = Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      threshold,
      keyColumnName,
      compareColumnName
    )
    println(result.prettyPrintResult())
  }

  @TaskAction
  fun compareBenchmarkCI() {

    fun getWrongResults(result: List<BenchmarkResult>): List<BenchmarkResult> =
      result.filter { it::class == BenchmarkResult.ERROR::class || it::class == BenchmarkResult.FAILED::class }

    val result: List<BenchmarkResult> = Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      threshold,
      keyColumnName,
      compareColumnName
    )
    println(result.prettyPrintResult())
    GithubIntegration.setCommentResult(result)
    val errors = getWrongResults(result)
    if (errors.nonEmpty())
      throw GradleException(errors.prettyPrintResult())
  }

}
