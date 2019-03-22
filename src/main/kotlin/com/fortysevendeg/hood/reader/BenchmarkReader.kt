package com.fortysevendeg.hood.reader

import arrow.data.ListK
import com.fortysevendeg.hood.Benchmark

interface BenchmarkReader {

  private fun <A, B> Map<A, List<Pair<A, List<B>>>>.mapValuesToSecond(): Map<A, List<B>> =
    this.mapValues { entry -> entry.value.flatMap { it.second } }

  fun ListK<Pair<String, ListK<Benchmark>>>.groupByBenchmarkKey(): Map<String, List<Benchmark>> =
    this.groupBy { it.first }.mapValuesToSecond()
}