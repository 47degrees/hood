package com.fortysevendeg.hood

import arrow.core.Option
import arrow.core.getOrElse
import arrow.data.extensions.list.foldable.exists
import arrow.data.extensions.list.foldable.forAll
import arrow.effects.IO
import arrow.effects.extensions.io.applicativeError.handleError
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import com.fortysevendeg.hood.reader.CsvBenchmarkReader
import java.io.File

object Comparator {

  private fun compare(previous: Benchmark, current: Benchmark, threshold: Double): BenchmarkResult =
    when {
      previous.score <= current.score             -> BenchmarkResult.OK
      previous.score - current.score <= threshold -> BenchmarkResult.WARN
      else                                        -> BenchmarkResult.FAILED
    }

  private fun getCompareResults(
    currentBenchmarks: Map<String, List<Benchmark>>,
    prev: Benchmark,
    threshold: Double
  ): List<Pair<List<Benchmark>, BenchmarkResult>> =
    currentBenchmarks.mapValues {
      it.value.filter { current -> prev.key == current.key }
    }.flatMap {
      val currentWithName: List<Benchmark> =
        it.value.map { current -> Benchmark(it.key, current.score, current.scoreError) }
      it.value.map { current -> Pair(currentWithName, compare(prev, current, threshold)) }
    }

  private fun buildBenchmarkComparison(
    key: String,
    previous: Benchmark,
    result: Pair<List<Benchmark>, BenchmarkResult>
  ): BenchmarkComparison {
    val benchmarksWithName = result.first.plus(previous).reversed()
    return BenchmarkComparison(key, benchmarksWithName, result.second)
  }

  fun compareCsv(
    previousBenchmarkFile: File,
    currentBenchmarkFiles: List<File>,
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    maybeThreshold: Option<Double>
  ): IO<List<BenchmarkComparison>> = fx {
    //List of BenchmarkComparison

    val previousBenchmarks: Pair<String, List<Benchmark>> =
      !CsvBenchmarkReader.readFilesToBenchmark(
        keyColumnName,
        compareColumnName,
        thresholdColumnName,
        previousBenchmarkFile
      ).map { it.entries.first().toPair() }

    val currentBenchmarks: Map<String, List<Benchmark>> =
      !CsvBenchmarkReader.readFilesToBenchmark(
        keyColumnName,
        compareColumnName,
        thresholdColumnName,
        *currentBenchmarkFiles.toTypedArray()
      )

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
        val previousWithName = Benchmark(previousBenchmarks.first, prev.score, prev.scoreError)

        getCompareResults(
          currentBenchmarks,
          prev,
          maybeThreshold.getOrElse { prev.scoreError }).map {
          buildBenchmarkComparison(prev.key, previousWithName, it)
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