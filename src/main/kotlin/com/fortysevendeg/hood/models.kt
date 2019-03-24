package com.fortysevendeg.hood

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.generic.coproduct2.Coproduct2
import java.io.File

enum class BenchmarkFileFormat {
  CSV, JSON;

  override fun toString(): String = super.toString().toLowerCase()

  companion object {

    private fun toFileFormat(str: String): Option<BenchmarkFileFormat> = when {
      str.toLowerCase() == CSV.toString()  -> BenchmarkFileFormat.CSV.some()
      str.toLowerCase() == JSON.toString() -> BenchmarkFileFormat.JSON.some()
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

  data class ERROR(val error: Throwable) : BenchmarkResult() {
    override fun symbol(): String = "☠"
    override fun icon(): String = ":skull_and_crossbones:"
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

data class JsonPrimaryMetric(
  val score: Double,
  val scoreError: Double,
  val scoreUnit: String,
  val rawData: List<List<Double>>
)

data class JsonSecondaryMetric(
  val score: Double?,
  val scoreError: Double?,
  val scoreUnit: String?,
  val rawData: List<List<Double>>?
)

data class JsonBenchmark(
  val benchmark: String,
  val mode: String,
  val primaryMetric: JsonPrimaryMetric,
  val secondaryMetrics: JsonSecondaryMetric
)

typealias Benchmark = Coproduct2<CsvBenchmark, JsonBenchmark>

data class BenchmarkComparison(
  val key: String,
  val benchmark: List<Benchmark>,
  val result: BenchmarkResult,
  val threshold: Double
)