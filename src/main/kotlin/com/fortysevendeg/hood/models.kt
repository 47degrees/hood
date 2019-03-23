package com.fortysevendeg.hood

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import java.io.File

data class Benchmark(
  val key: String,
  val score: Double,
  val scoreError: Double
)

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

enum class FileFormat {
  MD, JSON;

  override fun toString(): String {
    return super.toString().toLowerCase()
  }

  companion object {
    fun getFileFormat(file: File) = toFileFormat(file.extension.toLowerCase())

    fun toFileFormat(str: String): Option<FileFormat> = when {
      str.toLowerCase() == "md"   -> FileFormat.MD.some()
      str.toLowerCase() == "json" -> FileFormat.JSON.some()
      else                        -> None
    }
  }
}

data class BenchmarkComparison(
  val key: String,
  val benchmark: List<Benchmark>,
  val result: BenchmarkResult
)

object BenchmarkInconsistencyError :
  Throwable("Benchmarks have differents formats and cannot be compared")

enum class GhStatusState(val value: String) {
  Succeed("success"), Pending("pending"), Failed("failure")
}

//Github

data class GhInfo(val owner: String, val repo: String, val token: String)

data class GhStatus(
  val state: GhStatusState,
  val description: String,
  val context: String = "benchmark-ci/hood"
)

data class GhUser(val login: String)

data class GhComment(val id: Long, val user: GhUser, val body: String)

data class GhFileSha(val sha: String)

data class GhCreateCommit(val message: String, val content: String, val branch: String)

data class GhUpdateCommit(
  val message: String,
  val content: String,
  val sha: String,
  val branch: String
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