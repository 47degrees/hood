package com.fortysevendeg.hood

import arrow.generic.coproduct2.First
import arrow.generic.coproduct2.Second
import arrow.generic.coproduct2.fold

object BenchmarkCopHandler {

  fun benchmarkOf(csv: CsvBenchmark): Benchmark = First(csv)
  fun benchmarkOf(json: JsonBenchmark): Benchmark = Second(json)

  fun Benchmark.getKey(): String = this.fold(CsvBenchmark::key, JsonBenchmark::benchmark)
  fun Benchmark.getScore(): Double = this.fold(CsvBenchmark::score) { it.primaryMetric.score }
  fun Benchmark.getScoreError(): Double =
    this.fold(CsvBenchmark::scoreError) { it.primaryMetric.scoreError }

  fun Benchmark.withName(name: String): Benchmark =
    this.fold({ benchmarkOf(it.copy(key = name)) }, { benchmarkOf(it.copy(benchmark = name)) })

}