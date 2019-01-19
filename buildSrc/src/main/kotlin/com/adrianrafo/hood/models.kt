package com.adrianrafo.hood

data class Benchmark(
  val name: String,
  val score: Double
)

sealed class BenchmarkResult {
  abstract fun message(): String

  data class OK(val key: String) : BenchmarkResult() {
    override fun message() = "*** $key looks good ***"
  }

  data class WARN(val key: String) : BenchmarkResult() {
    override fun message() = "*** $key is slightly worst, but it's ok ***"
  }

  data class FAILED(val name: String) : BenchmarkResult() {
    override fun message() = "*** $name doesn't look good, nice try ***"
  }

  data class ERROR(val error: Throwable) : BenchmarkResult() {
    override fun message() = "***Error: ${error.message} ***"
  }
}

object BenchmarkInconsistencyError :
  Throwable("Benchmarks have differents formats and cannot be compared")