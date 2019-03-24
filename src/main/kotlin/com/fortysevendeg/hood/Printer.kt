package com.fortysevendeg.hood

import arrow.generic.coproduct2.select
import com.fortysevendeg.hood.models.Benchmark
import com.fortysevendeg.hood.models.BenchmarkComparison
import com.fortysevendeg.hood.models.BenchmarkResult
import com.fortysevendeg.hood.models.OutputFileFormat
import org.http4k.format.Jackson

//Use only on reading input phase
fun cleanKey(key: String): String = key.substringAfterLast('.').toLowerCase().capitalize()

private fun List<Benchmark>.mkString() =
  this.joinToString(separator = "\n") { "${it.getKey()} | ${it.getScore()}" }

/**
 * Expected format
 * Icon `benchmark key` (Threshold)
 *
 * Benchmark | Value
 * file name | benchmark.score
 * file name | benchmark.score
 * file name | benchmark.score
 *
 * result
 */
fun List<BenchmarkComparison>.prettyPrintResult(): String =
  this.joinToString(separator = "\n\n") { comp ->
    val header =
      "${comp.result.symbol()} `${comp.key.capitalize()}` (Threshold: ${comp.threshold})"
    if (comp.result is BenchmarkResult.ERROR)
      """
        |$header
        |Error: ${comp.result.error.message}
        |""".trimMargin()
    else
      """
        |$header
        |${comp.benchmarks.mkString()}
        |""".trimMargin()
  }

private fun List<BenchmarkComparison>.printMDFormat(): String =
  this.joinToString(separator = "\n\n") { bc ->
    val header = "${bc.result.icon()} `${bc.key.capitalize()}` (Threshold: ${bc.threshold})"
    if (bc.result is BenchmarkResult.ERROR)
      """
        |$header
        |Error: ${bc.result.error.message}
        |""".trimMargin()
    else
      """
        |$header
        |
        |Benchmark | Value
        ||--|--|
        |${bc.benchmarks.mkString()}
        |""".trimMargin()
  }

//Same format as input json but with the same key group as MD print(using symbol)
private fun List<BenchmarkComparison>.printJSONFormat(): String =
  JsonSupport.mapper.writeValueAsString(Jackson.array(this.flatMap { bc ->
    bc.benchmarks.map { it.select(Unit) }.map { maybeBenchmark ->
      maybeBenchmark.fold(Jackson::obj) { jsonBenchmark ->
        Jackson {
          obj(
            "benchmark" to string("${bc.result.symbol()} ${bc.key}.${jsonBenchmark.benchmark}"),
            "mode" to string(jsonBenchmark.mode),
            "primaryMetric" to obj(
              "score" to number(jsonBenchmark.primaryMetric.score),
              "scoreError" to number(jsonBenchmark.primaryMetric.scoreError),
              "scoreUnit" to string(jsonBenchmark.primaryMetric.scoreUnit),
              "rawData" to array(jsonBenchmark.primaryMetric.rawData.map { arr -> array(arr.map(this::number)) })
            ),
            "secondaryMetrics" to obj()
          )
        }
      }
    }
  }))

fun List<BenchmarkComparison>.prettyPrintResult(format: OutputFileFormat): String =
  if (format == OutputFileFormat.MD) printMDFormat()
  else printJSONFormat()
