package com.adrianrafo.hood

data class Benchmark(
  val name: String,
  val mode: String,
  val treads: Int,
  val samples: Int,
  val score: Double,
  val scoreError: Double,
  val unit: String
)

data class HeliosBenchmark(val decoding: Benchmark, val parsing: Benchmark)

enum class BenchmarkResult {
  IMPROVED, WARN, ERROR
}