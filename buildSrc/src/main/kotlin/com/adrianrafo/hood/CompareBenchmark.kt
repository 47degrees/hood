package com.adrianrafo.hood

import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.applicativeError.handleError
import arrow.effects.instances.io.monad.monad
import arrow.instances.list.foldable.exists
import arrow.instances.list.foldable.forAll
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
  fun compareBenchmark(): Unit {

    fun compare(previous: Benchmark, current: Benchmark): BenchmarkResult =
      when {
        previous.score <= current.score             -> BenchmarkResult.OK(current.name)
        previous.score - current.score <= threshold -> BenchmarkResult.WARN(current.name)
        else                                        -> BenchmarkResult.FAILED(current.name)
      }

    val result: List<BenchmarkResult> = IO.monad().binding<List<BenchmarkResult>> {

      val previousBenchmarks: List<Benchmark> =
        BenchmarkReader.read(previousBenchmarkPath, keyColumnName, compareColumnName).bind()
      val currentBenchmarks: List<Benchmark> =
        BenchmarkReader.read(currentBenchmarkPath, keyColumnName, compareColumnName).bind()
      if (previousBenchmarks.forAll { prev -> currentBenchmarks.exists { it.name == prev.name } })
        previousBenchmarks.flatMap { previous ->
          currentBenchmarks.flatMap { current ->
            if (previous.name == current.name)
              listOf(compare(previous, current))
            else listOf<BenchmarkResult>()
          }
        }
      else listOf<BenchmarkResult>(BenchmarkResult.ERROR(BenchmarkInconsistencyError))
    }.fix().handleError {
      listOf(BenchmarkResult.ERROR(it))
    }.unsafeRunSync()
    println(result)
  }

}
