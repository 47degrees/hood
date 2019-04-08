package com.fortysevendeg.hood.reader

import arrow.data.ListK

interface BenchmarkReader {

  fun <A, B> ListK<Pair<A, ListK<B>>>.groupByKey(): Map<A, List<B>> =
    this.groupBy { it.first }.mapValues { entry -> entry.value.flatMap { it.second } }

}