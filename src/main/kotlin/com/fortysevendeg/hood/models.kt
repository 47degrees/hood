package com.fortysevendeg.hood

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

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
  Throwable("Benchmarks have differents formats and cannot be compared")

enum class GhStatusState(val value: String) {
  Succeed("success"), Pending("pending"), Failed("failure")
}

//Benchmarks

@JsonIgnoreProperties(value = ["key", "score", "scoreError"])
abstract class Benchmark(
  open val key: String,
  open val score: Double,
  open val scoreError: Double
)

data class BenchmarkComparison(
  val key: String,
  val benchmark: List<Benchmark>,
  val result: BenchmarkResult
)

data class CsvBenchmark(
  override val key: String,
  override val score: Double,
  override val scoreError: Double
) : Benchmark(
  key,
  score,
  scoreError
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
) : Benchmark(
  benchmark,
  primaryMetric.score,
  primaryMetric.scoreError
)