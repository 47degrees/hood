package com.fortysevendeg.hood

import arrow.generic.coproduct2.First
import arrow.generic.coproduct2.Second
import arrow.generic.coproduct2.fold
import com.fortysevendeg.hood.models.Benchmark
import com.fortysevendeg.hood.models.CsvBenchmark
import com.fortysevendeg.hood.models.JsonBenchmark

fun benchmarkOf(csv: CsvBenchmark): Benchmark = First(csv)
fun benchmarkOf(json: JsonBenchmark): Benchmark = Second(json)

fun Benchmark.getKey(): String = this.fold(CsvBenchmark::key, JsonBenchmark::benchmark)
fun Benchmark.getScore(): Double = this.fold(CsvBenchmark::score) { it.primaryMetric.score }
fun Benchmark.getScoreError(): Double =
  this.fold(CsvBenchmark::scoreError) { it.primaryMetric.scoreError }

fun Benchmark.withName(name: String): Benchmark =
  this.fold({ benchmarkOf(it.copy(key = name)) }, { benchmarkOf(it.copy(benchmark = name)) })

fun Benchmark.withThreshold(threshold: Double): Benchmark =
  this.fold(
    { benchmarkOf(it.copy(scoreError = threshold)) },
    { benchmarkOf(it.copy(primaryMetric = it.primaryMetric.copy(scoreError = threshold))) })
