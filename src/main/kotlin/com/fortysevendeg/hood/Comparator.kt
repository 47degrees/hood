package com.fortysevendeg.hood

import arrow.core.*
import arrow.data.extensions.list.foldable.exists
import arrow.data.extensions.list.foldable.forAll
import arrow.data.extensions.list.semigroup.plus
import arrow.data.foldLeft
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.extensions.io.applicativeError.handleError
import arrow.effects.extensions.io.fx.fx
import com.fortysevendeg.hood.models.*
import com.fortysevendeg.hood.reader.CsvBenchmarkReader
import com.fortysevendeg.hood.reader.JsonBenchmarkReader
import java.io.File

object Comparator {

  //Compare 2 benchmarks and get its result: Ok, Warning or Failure
  private fun compare(
    previous: Benchmark,
    current: Benchmark,
    threshold: Double
  ): BenchmarkResult =
    when {
      previous.getScore() <= current.getScore()             -> BenchmarkResult.OK
      previous.getScore() - current.getScore() <= threshold -> BenchmarkResult.WARN
      else                                                  -> BenchmarkResult.FAILED
    }

  /**
   * Get the branch benchmarks grouped by file name and one of the master benchmarks
   * Filter out the keys non existing on master benchmark
   * Compare and get the result
   * @return A list of benchmarks with the file name as key and their result of compare them with the master benchmark
   */
  private fun getCompareResults(
    currentBenchmarks: Map<String, List<Benchmark>>,
    prev: Benchmark,
    threshold: Double
  ): List<Pair<List<Benchmark>, BenchmarkResult>> =
    currentBenchmarks.mapValues {
      it.value.filter { current -> prev.getKey() == current.getKey() }
    }.flatMap { entry ->
      val currentWithName: List<Benchmark> =
        entry.value.map { it.withName(entry.key) }
      entry.value.map { Pair(currentWithName, compare(prev, it, threshold)) }
    }

  /**
   * Build the comparison result with the master score on the first position
   */
  private fun buildBenchmarkComparison(
    key: String,
    previous: Benchmark,
    result: Pair<List<Benchmark>, BenchmarkResult>
  ): BenchmarkComparison = BenchmarkComparison(
    key,
    result.first.plus(previous).reversed(),
    result.second,
    previous.getScoreError()
  )

  /**
   * Use csv or json reader depending on the file extension
   */
  private fun readFilesToBenchmark(
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    vararg benchmarkFiles: File
  ): IO<Map<String, List<Benchmark>>> = fx {
    val (csvFiles, jsonFiles) = benchmarkFiles.partition { file ->
      BenchmarkFileFormat.getFileFormat(file).exists { it == BenchmarkFileFormat.CSV }
    }

    val csvBenchmarks = !CsvBenchmarkReader.readFilesToBenchmark(
      keyColumnName,
      compareColumnName,
      thresholdColumnName,
      *csvFiles.toTypedArray()
    ).map { map ->
      map.mapValues { entry -> entry.value.map(::benchmarkOf) }
    }

    val jsonBenchmarks =
      !JsonBenchmarkReader.readFilesToBenchmark(*jsonFiles.toTypedArray()).map { map ->
        map.mapValues { entry -> entry.value.map(::benchmarkOf) }
      }

    csvBenchmarks.foldLeft(jsonBenchmarks) { map, entry ->
      map[entry.key].toOption().fold({ map.plus(entry.toPair()) }) { json ->
        val fullEntry = entry.toPair().copy(second = entry.value.plus(json))
        map.plus(fullEntry)
      }
    }

  }

  /**
   * Select threshold between all the possibilities
   * If there is benchmark threshold we'll use that.
   * If not we use the general.
   * If none are defined, is used the one in the benchmark.
   */
  private fun selectThreshold(
    masterBenchmark: Benchmark,
    generalThreshold: Option<Double>,
    benchmarkThreshold: Option<Map<String, Double>>
  ): Double {
    val general = generalThreshold.getOrElse { masterBenchmark.getScoreError() }
    return benchmarkThreshold.flatMap { it[masterBenchmark.getKey()].toOption() }
      .getOrElse { general }
  }

  fun compareBenchmarks(
    previousBenchmarkFile: File,
    currentBenchmarkFiles: List<File>,
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    maybeGeneralThreshold: Option<Double>,
    maybeBenchmarkThreshold: Option<Map<String, Double>>,
    includeRegex: Option<Regex>,
    excludeRegex: Option<Regex>
  ): IO<Either<BenchmarkComparisonError, List<BenchmarkComparison>>> = fx {

    val previousBenchmarks: Pair<String, List<Benchmark>> =
      !readFilesToBenchmark(
        keyColumnName,
        compareColumnName,
        thresholdColumnName,
        previousBenchmarkFile
      ).map { it.entries.first().toPair() }

    val currentBenchmarks: Map<String, List<Benchmark>> =
      !readFilesToBenchmark(
        keyColumnName,
        compareColumnName,
        thresholdColumnName,
        *currentBenchmarkFiles.toTypedArray()
      )

    val previousBenchmarksAfterFilter: Pair<String, List<Benchmark>> =
      previousBenchmarks.copy(
        second = previousBenchmarks.second.filterBenchmarkIE(includeRegex, excludeRegex)
      )
    val currentBenchmarksAfterFilter: Map<String, List<Benchmark>> =
      currentBenchmarks.mapValues { it.value.filterBenchmarkIE(includeRegex, excludeRegex) }

    val currentKeys = currentBenchmarksAfterFilter.values.toList().flatMap { list -> list.map { it.getKey() } }

    val inconsistencies = previousBenchmarksAfterFilter.second.map { it.getKey() }.minus(currentKeys)

    println("[WARN] Found inconsistencies between benchmarks, only common keys will be compared.\n  Inconsistencies: $inconsistencies")

    previousBenchmarksAfterFilter.second.flatMap { prev ->
      val threshold = selectThreshold(prev, maybeGeneralThreshold, maybeBenchmarkThreshold)
      val previousModified =
        prev.withName(previousBenchmarksAfterFilter.first).withThreshold(threshold)

      getCompareResults(currentBenchmarksAfterFilter, prev, threshold).map {
        buildBenchmarkComparison(prev.getKey(), previousModified, it)
      }
    }.right()

  }.fix().handleError {
    BenchmarkComparisonError(it).left()
  }

}
