package com.fortysevendeg.hood.reader

import arrow.core.Option
import arrow.core.extensions.option.fx.fx
import arrow.core.firstOrNone
import arrow.core.fix
import arrow.data.ListK
import arrow.data.extensions.list.traverse.traverse
import arrow.data.fix
import arrow.data.k
import arrow.effects.IO
import arrow.effects.extensions.io.applicative.applicative
import arrow.effects.fix
import arrow.syntax.collections.tail
import com.fortysevendeg.hood.Benchmark
import com.fortysevendeg.hood.BenchmarkInconsistencyError
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader

object CsvBenchmarkReader : BenchmarkReader {

  private fun benchmarkFromCSV(row: CSVRecord, key: Int, column: Int, threshold: Int): Benchmark =
    Benchmark(
      row[key].substringAfterLast('.'),
      row[column].toDouble(),
      row[threshold].toDouble()
    )

  private fun List<CSVRecord>.getColumnIndex(columnName: String): Option<Int> =
    this.firstOrNone().map { it.indexOf(columnName) }

  private fun readCSV(
    file: FileReader,
    keyColumn: String,
    compareColumn: String,
    thresholdColumn: String
  ): IO<ListK<Benchmark>> =
    IO {
      CSVParser(
        file,
        CSVFormat.DEFAULT.withTrim()
      )
    }.bracket({ csvParser -> IO { csvParser.close() } }) { csvParser ->
      IO {
        val records: List<CSVRecord> = csvParser.records
        fx {
          val key = !records.getColumnIndex(keyColumn)
          val column = !records.getColumnIndex(compareColumn)
          val threshold = !records.getColumnIndex(thresholdColumn)

          records.tail().map {
            benchmarkFromCSV(
              it,
              key,
              column,
              threshold
            )
          }
        }.fix()
      }.flatMap {
        it.fold({ IO.raiseError<ListK<Benchmark>>(BenchmarkInconsistencyError) },
          { IO { it.k() } })
      }
    }

  fun readFilesToBenchmark(
    keyColumn: String,
    compareColumn: String,
    thresholdColumn: String,
    vararg files: File
  ): IO<Map<String, List<Benchmark>>> =
    files.toList().traverse(
      IO.applicative()
    ) { file ->
      IO { FileReader(file) }.flatMap { fileReader ->
        readCSV(
          fileReader,
          keyColumn,
          compareColumn,
          thresholdColumn
        ).map { file.nameWithoutExtension to it }
      }
    }.fix().map { it.fix().groupByBenchmarkKey() }

}