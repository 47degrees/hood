package com.adrianrafo.hood

import arrow.data.k
import arrow.instances.list.applicative.map2
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
  var maxDiffScore = project.objects.property<Int>().getOrElse(50)

  @TaskAction
  fun compareBenchmark(): Unit {
    //IO.monad().binding {
    val previousBenchmark: List<Benchmark> =
      BenchmarkReader.read(previousBenchmarkPath, keyColumnName, compareColumnName).unsafeRunSync()
    val currentBenchmark: List<Benchmark> =
      BenchmarkReader.read(currentBenchmarkPath, keyColumnName, compareColumnName).unsafeRunSync()
    println("Previous: $previousBenchmark")
    println("Current: $currentBenchmark")
    previousBenchmark.k().map2(), current)
    when {
      previousBenchmark.score <= currentBenchmark.score             -> println("*** Commit looks good ***")
      previousBenchmark.score - currentBenchmark.score <= threshold -> println("*** Commit is slightly worst, but it's ok ***")
      else                                                          -> println("*** Commit doesn't look good, nice try ***")
    }
    //}.fix().unsafeRunSync()
  }
}
