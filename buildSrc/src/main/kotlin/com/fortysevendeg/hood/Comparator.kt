package com.fortysevendeg.hood

import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.applicativeError.handleError
import arrow.effects.instances.io.monad.monad
import arrow.instances.list.foldable.exists
import arrow.instances.list.foldable.forAll
import java.io.File

object Comparator {

  private fun compare(previous: Benchmark, current: Benchmark, threshold: Int): BenchmarkResult =
    when {
      previous.score <= current.score             -> BenchmarkResult.OK(current.key)
      previous.score - current.score <= threshold -> BenchmarkResult.WARN(current.key)
      else                                        -> BenchmarkResult.FAILED(current.key)
    }

  fun compareCsv(
    previousBenchmarkPath: String,
    currentBenchmarkPaths: List<String>,
    threshold: Int,
    keyColumnName: String,
    compareColumnName: String
  ): IO<List<BenchmarkComparison>> = IO.monad().binding {
    //List of BenchmarkComparison

    val previousBenchmarks: Pair<String, List<Benchmark>> =
      BenchmarkReader.readPath(previousBenchmarkPath, keyColumnName, compareColumnName).bind()

    val currentBenchmarkFiles: List<File> = IO { currentBenchmarkPaths.map { File(it) } }.bind()
    val currentBenchmarks: Map<String, List<Benchmark>> =
      BenchmarkReader.readFiles(currentBenchmarkFiles, keyColumnName, compareColumnName).bind()

    val isConsistent = previousBenchmarks.second.forAll { prev ->
      currentBenchmarks.values.toList()
        .forAll {
          it.exists { current ->
            current.key.substringAfterLast('.') == prev.key.substringAfterLast('.')
          }
        }
    }

    if (isConsistent)
      previousBenchmarks.second.flatMap { prev ->
        val previousWithName = Benchmark(previousBenchmarks.first, prev.score)

        val results: List<Pair<List<Benchmark>, BenchmarkResult>> =
          currentBenchmarks.mapValues {
            it.value.filter { current ->              prev.key == current.key            }
          }.flatMap {
            val currentWithName: List<Benchmark> =
              it.value.map { current -> Benchmark(it.key, current.score) }
            it.value.map { current -> Pair(currentWithName, compare(prev, current, threshold)) }
          }

        results.map {
          val benchmarkWithName = it.first.plus(previousWithName).reversed()
          BenchmarkComparison(prev.key, benchmarkWithName, it.second)
        }
      }
    else listOf(
      BenchmarkComparison(
        "",
        emptyList(),
        BenchmarkResult.ERROR(BenchmarkInconsistencyError)
      )
    )
  }.fix().handleError { listOf(BenchmarkComparison("", emptyList(), BenchmarkResult.ERROR(it))) }

}