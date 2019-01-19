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
    IO.monad().binding {
      val previousBenchmarks: List<Benchmark> =
        BenchmarkReader.read(previousBenchmarkPath, keyColumnName, compareColumnName).bind()
      val currentBenchmarks: List<Benchmark> =
        BenchmarkReader.read(currentBenchmarkPath, keyColumnName, compareColumnName).bind()

      if (previousBenchmarks.forAll { prev -> currentBenchmarks.exists { it.name == prev.name } })
        previousBenchmarks.flatMap { previous ->
          currentBenchmarks.map { current ->
            if (previous.name == current.name)
              compare(previous, current)
          }
        }
      else IO.raiseError<List<Unit>>(BenchmarkInconsistencyError).bind()

    }.fix().handleError {
      println("Error: ${it.message}")
      listOf()
    }.unsafeRunSync()
  }

  private fun compare(previous:Benchmark, current:Benchmark) =
    when {
      previous.score <= current.score             -> println("*** ${current.name} looks good ***")
      previous.score - current.score <= threshold -> println("*** ${current.name} is slightly worst, but it's ok ***")
      else                                        -> println("*** ${current.name} doesn't look good, nice try ***")
    }
}
