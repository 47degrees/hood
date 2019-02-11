package com.fortysevendeg.hood

data class Benchmark(
  val key: String,
  val score: Double
)

sealed class BenchmarkResult {
  abstract fun message(): String

  data class OK(val key: String) : BenchmarkResult() {
    override fun message(): String = "*** $key looks good ***"
  }

  data class WARN(val key: String) : BenchmarkResult() {
    override fun message(): String = "*** $key is slightly worst, but it's ok ***"
  }

  data class FAILED(val key: String) : BenchmarkResult() {
    override fun message(): String = "*** $key doesn't look good, nice try ***"
  }

  data class ERROR(val error: Throwable) : BenchmarkResult() {
    override fun message(): String = "*** Error: ${error.message} ***"
  }

}

data class BenchmarkComparison(
  val key: String,
  val benchmark: List<Benchmark>,
  val result: BenchmarkResult
)

/**
 * Expected format
 * `benchmark key`
 * file name | benchmark.score
 * file name | benchmark.score
 * file name | benchmark.score
 * result
 */
fun List<BenchmarkComparison>.prettyPrintResult(): String =
  this.joinToString(separator = "\n\n") { comp ->
    """
    |`${comp.key.capitalize()}`
    |${comp.benchmark.joinToString(separator = "\n") { "${it.key} | ${it.score}" }}
    |${comp.result.message()}
  """.trimMargin()
  }

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
