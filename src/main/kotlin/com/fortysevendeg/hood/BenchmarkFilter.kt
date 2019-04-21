package com.fortysevendeg.hood

import arrow.core.Option
import com.fortysevendeg.hood.models.Benchmark

private fun List<Benchmark>.filterUsingRegex(
  maybeRegex: Option<Regex>,
  rFilter: ((Benchmark) -> (Boolean)) -> List<Benchmark>
): List<Benchmark> =
  maybeRegex.fold({ this }) { regex -> rFilter { bm: Benchmark -> regex.matches(bm.getKey()) } }

fun List<Benchmark>.filterBenchmark(includeRegex: Option<Regex>): List<Benchmark> =
  filterUsingRegex(includeRegex) { this.filter(it) }

fun List<Benchmark>.filterBenchmarkNot(excludeRegex: Option<Regex>): List<Benchmark> =
  filterUsingRegex(excludeRegex) { this.filterNot(it) }

fun List<Benchmark>.filterBenchmarkIE(
  includeRegex: Option<Regex>,
  excludeRegex: Option<Regex>
): List<Benchmark> = filterBenchmark(includeRegex).filterBenchmarkNot(excludeRegex)
