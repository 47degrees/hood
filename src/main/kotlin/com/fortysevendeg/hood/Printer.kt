package com.fortysevendeg.hood

import com.fortysevendeg.hood.BenchmarkCopHandler.getKey
import com.fortysevendeg.hood.BenchmarkCopHandler.getScore

object Printer {

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
        |${comp.benchmark.mkString()}
        |""".trimMargin()
    }

  private fun List<BenchmarkComparison>.printMDFormat(): String =
    this.joinToString(separator = "\n\n") { comp ->
      val header = "${comp.result.icon()} `${comp.key.capitalize()}` (Threshold: ${comp.threshold})"
      if (comp.result is BenchmarkResult.ERROR)
        """
        |$header
        |Error: ${comp.result.error.message}
        |""".trimMargin()
      else
        """
        |$header
        |
        |Benchmark | Value
        ||--|--|
        |${comp.benchmark.mkString()}
        |""".trimMargin()
    }

  //Same format as input json but with the same key grouped as MD print(without icon)
  //Custom serializer
  private fun List<BenchmarkComparison>.printJSONFormat(): String = TODO()

  fun List<BenchmarkComparison>.prettyPrintResult(format: OutputFileFormat): String =
    if (format == OutputFileFormat.MD) printMDFormat()
    else printJSONFormat()

}