package com.fortysevendeg.hood

object Printer {

  private fun List<Benchmark>.mkString() =
    this.joinToString(separator = "\n") { "${it.key} | ${it.score}" }

  /**
   * Expected format
   * `benchmark key`
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
      val header = "${comp.result.symbol()} `${comp.key.capitalize()}`"
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
      val header = "${comp.result.icon()} `${comp.key.capitalize()}`"
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

  private fun List<BenchmarkComparison>.printJSONFormat(): String = TODO()

  fun List<BenchmarkComparison>.prettyPrintResult(format: FileFormat): String {

    return if (format == FileFormat.MD) printMDFormat()
    else printJSONFormat()
  }

}