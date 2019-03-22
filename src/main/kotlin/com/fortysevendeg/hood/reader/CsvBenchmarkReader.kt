package com.fortysevendeg.hood.reader

import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.fix
import arrow.data.ListK
import arrow.data.fix
import arrow.data.k
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.applicative.applicative
import arrow.instances.list.traverse.traverse
import arrow.instances.option.monad.monad
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
        Option.monad().binding {
          val key = records.getColumnIndex(keyColumn).bind()
          val column = records.getColumnIndex(compareColumn).bind()
          val threshold = records.getColumnIndex(thresholdColumn).bind()

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