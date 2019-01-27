package com.adrianrafo.hood

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class CompareBenchmark : DefaultTask() {

  @get:Input
  var previousBenchmarkPath = project.objects.property<String>().getOrElse("master.csv")
  @get:Input
  var currentBenchmarkPath = project.objects.property<String>().getOrElse("current.csv")
  @get:Input
  var keyColumnName = project.objects.property<String>().getOrElse("Benchmark")
  @get:Input
  var compareColumnName = project.objects.property<String>().getOrElse("Score")
  @get:Input
  var threshold = project.objects.property<Int>().getOrElse(50)

  @TaskAction
  fun compareBenchmark() {
    val result: List<BenchmarkResult> = Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      threshold,
      keyColumnName,
      compareColumnName
    )
    println(result)
  }

  @TaskAction
  fun compareBenchmarkCI() {
    val result: List<BenchmarkResult> = Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      threshold,
      keyColumnName,
      compareColumnName
    )
    //TODO set github pr status based on result
    println(result)
  }

}
