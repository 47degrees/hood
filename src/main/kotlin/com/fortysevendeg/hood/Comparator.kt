package com.fortysevendeg.hood

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import arrow.data.extensions.list.foldable.exists
import arrow.data.extensions.list.foldable.forAll
import arrow.data.extensions.list.semigroup.plus
import arrow.data.foldLeft
import arrow.effects.IO
import arrow.effects.extensions.io.applicativeError.handleError
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import com.fortysevendeg.hood.BenchmarkCopHandler.benchmarkOf
import com.fortysevendeg.hood.BenchmarkCopHandler.getKey
import com.fortysevendeg.hood.BenchmarkCopHandler.getScore
import com.fortysevendeg.hood.BenchmarkCopHandler.getScoreError
import com.fortysevendeg.hood.BenchmarkCopHandler.withName
import com.fortysevendeg.hood.reader.CsvBenchmarkReader
import com.fortysevendeg.hood.reader.JsonBenchmarkReader
import java.io.File

object Comparator {

  private fun compare(
    previous: Benchmark,
    current: Benchmark,
    threshold: Double
  ): BenchmarkResult =
    when {
      previous.getScore() <= current.getScore() -> BenchmarkResult.OK
      previous.getScore() - current.getScore() <= threshold -> BenchmarkResult.WARN
      else -> BenchmarkResult.FAILED
    }

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

  private fun buildBenchmarkComparison(
    key: String,
    previous: Benchmark,
    result: Pair<List<Benchmark>, BenchmarkResult>
  ): BenchmarkComparison {
    val benchmarksWithName = result.first.plus(previous).reversed()
    return BenchmarkComparison(
      key,
      benchmarksWithName,
      result.second,
      previous.getScoreError()
    )
  }

  private fun readFilesToBenchmark(
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    vararg benchmarkFiles: File
  ): IO<Map<String, List<Benchmark>>> = fx {
    val (csvFiles, jsonFiles) = benchmarkFiles.partition { file ->
      BenchmarkFileFormat.getFileFormat(file).map { it == BenchmarkFileFormat.CSV }
        .getOrElse { false }
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
      map[entry.key].toOption().map { json ->
        val fullEntry = entry.toPair().copy(second = entry.value.plus(json))
        map.plus(fullEntry)
      }.getOrElse { map.plus(entry.toPair()) }
    }

  }

  fun compareBenchmarks(
    previousBenchmarkFile: File,
    currentBenchmarkFiles: List<File>,
    keyColumnName: String,
    compareColumnName: String,
    thresholdColumnName: String,
    maybeThreshold: Option<Double>
  ): IO<List<BenchmarkComparison>> = fx {

    //TODO Choose csv or json reader per file
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

    val isConsistent =
      previousBenchmarks.second.map { it.getKey() }.forAll { prevKey ->
        currentBenchmarks.values.toList().forAll { list ->
          list.map { it.getKey() }.exists { currentKey ->
            currentKey.substringAfterLast('.') == prevKey.substringAfterLast('.')
          }
        }
      }

    if (isConsistent)
      previousBenchmarks.second.flatMap { prev ->
        val previousWithName = prev.withName(previousBenchmarks.first)

        getCompareResults(
          currentBenchmarks,
          prev,
          maybeThreshold.getOrElse { prev.getScoreError() }).map {
          buildBenchmarkComparison(prev.getKey(), previousWithName, it)
        }
      }
    else listOf(
      BenchmarkComparison(
        "",
        emptyList(),
        BenchmarkResult.ERROR(BenchmarkInconsistencyError),
        maybeThreshold.getOrElse { 0.0 }
      )
    )
  }.fix().handleError {
    listOf(
      BenchmarkComparison(
        "",
        emptyList(),
        BenchmarkResult.ERROR(it),
        maybeThreshold.getOrElse { 0.0 })
    )
  }

}