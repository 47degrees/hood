package com.fortysevendeg.hood

import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.applicativeError.handleError
import arrow.effects.instances.io.monad.monad
import arrow.instances.list.foldable.exists
import arrow.instances.list.foldable.forAll

object Comparator {

  fun compareCsv(
    previousBenchmarkPath: String,
    currentBenchmarkPath: String,
    threshold: Int,
    keyColumnName: String,
    compareColumnName: String
  ): IO<List<BenchmarkResult>> {

    fun compare(previous: Benchmark, current: Benchmark): BenchmarkResult =
      when {
        previous.score <= current.score             -> BenchmarkResult.OK(current.name)
        previous.score - current.score <= threshold -> BenchmarkResult.WARN(current.name)
        else                                        -> BenchmarkResult.FAILED(current.name)
      }

    return IO.monad().binding {

      val previousBenchmarks: List<Benchmark> =
        BenchmarkReader.read(previousBenchmarkPath, keyColumnName, compareColumnName).bind()
      val currentBenchmarks: List<Benchmark> =
        BenchmarkReader.read(currentBenchmarkPath, keyColumnName, compareColumnName).bind()

      if (previousBenchmarks.forAll { prev -> currentBenchmarks.exists { it.name == prev.name } })
        previousBenchmarks.flatMap { previous ->
          currentBenchmarks.flatMap { current ->
            if (previous.name == current.name)
              listOf(compare(previous, current))
            else listOf()
          }
        }
      else listOf<BenchmarkResult>(BenchmarkResult.ERROR(BenchmarkInconsistencyError))

    }.fix().handleError { listOf(BenchmarkResult.ERROR(it)) }
  }
}