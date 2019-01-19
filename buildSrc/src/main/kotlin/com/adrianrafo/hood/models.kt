package com.adrianrafo.hood

data class Benchmark(
  val name: String,
  val score: Double
)

enum class BenchmarkResult {
  IMPROVED, WARN, ERROR
}