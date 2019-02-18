package com.fortysevendeg.hood

import arrow.core.None
import arrow.core.Option
import arrow.core.some

object syntax {

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
      """
      |`${comp.key.capitalize()}`
      |${comp.benchmark.joinToString(separator = "\n") { "${it.key} | ${it.score}" }}
      |${comp.result.message()}
      |""".trimMargin()
    }

  private fun List<BenchmarkComparison>.printMDFormat(): String =
    this.joinToString(separator = "\n\n") { comp ->
      """
      |`${comp.key.capitalize()}`
      |
      |Benchmark | Value
      ||--|--|
      |${comp.benchmark.joinToString(separator = "\n") { "${it.key} | ${it.score}" }}
      |
      |${comp.result.message()}
      |""".trimMargin()
    }

  private fun List<BenchmarkComparison>.printJSONFormat(): String = TODO()

  fun List<BenchmarkComparison>.prettyPrintResult(format: FileFormat): String {

    return if (format == FileFormat.MD) printMDFormat()
    else printJSONFormat()
  }

  fun String.toFileFormat(): Option<FileFormat> = when {
    this.toLowerCase() == "md"   -> FileFormat.MD.some()
    this.toLowerCase() == "json" -> FileFormat.JSON.some()
    else                         -> None
  }

}