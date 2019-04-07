package com.fortysevendeg.hood.models

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.generic.coproduct2.Coproduct2
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.File

enum class BenchmarkFileFormat {
  CSV, JSON;

  override fun toString(): String = super.toString().toLowerCase()

  companion object {

    private fun toFileFormat(str: String): Option<BenchmarkFileFormat> = when {
      str.toLowerCase() == CSV.toString()  -> CSV.some()
      str.toLowerCase() == JSON.toString() -> JSON.some()
      else                                 -> None
    }

    fun getFileFormat(file: File): Option<BenchmarkFileFormat> =
      toFileFormat(file.extension.toLowerCase())

  }
}

sealed class BenchmarkResult {
  abstract fun symbol(): String
  abstract fun icon(): String

  object OK : BenchmarkResult() {
    override fun symbol(): String = "✓"
    override fun icon(): String = ":heavy_check_mark:"
  }

  object WARN : BenchmarkResult() {
    override fun symbol(): String = "⚠"
    override fun icon(): String = ":warning:"
  }

  object FAILED : BenchmarkResult() {
    override fun symbol(): String = "✗"
    override fun icon(): String = ":red_circle:"
  }

}

object BenchmarkInconsistencyError :
  Throwable("Benchmarks have different formats and cannot be compared")

enum class GhStatusState(val value: String) {
  Succeed("success"), Pending("pending"), Failed("failure")
}

//Benchmarks

data class CsvBenchmark(
  val key: String,
  val score: Double,
  val scoreError: Double
)

//Json benchmarks

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonPrimaryMetric(
  val score: Double,
  val scoreError: Double,
  val scoreUnit: String,
  val rawData: List<List<Double>>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonSecondaryMetric(
  val score: Double?,
  val scoreError: Double?,
  val scoreUnit: String?,
  val rawData: List<List<Double>>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonBenchmark(
  val benchmark: String,
  val mode: String,
  val primaryMetric: JsonPrimaryMetric,
  val secondaryMetrics: JsonSecondaryMetric
)

typealias Benchmark = Coproduct2<CsvBenchmark, JsonBenchmark>

data class BenchmarkComparison(
  val key: String,
  val benchmarks: List<Benchmark>,
  val result: BenchmarkResult,
  val threshold: Double
)

data class BenchmarkComparisonError(val error: Throwable) {
  fun symbol(): String = "☠"
}