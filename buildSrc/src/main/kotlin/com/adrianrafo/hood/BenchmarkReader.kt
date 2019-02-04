package com.adrianrafo.hood

import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.fix
import arrow.effects.IO
import arrow.instances.option.monad.monad
import arrow.syntax.collections.tail
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.FileReader

object BenchmarkReader {

  private fun benchmarkFromCSV(row: CSVRecord, key: Int, column: Int): Benchmark = Benchmark(
    row[key],
    row[column].toDouble()
  )

  private fun List<CSVRecord>.getColumnIndex(columnName: String): Option<Int> =
    this.firstOrNone().map { it.indexOf(columnName) }

  fun read(path: String, keyColumn: String, compareColumn: String): IO<List<Benchmark>> =
    IO {
      CSVParser(
        FileReader(path),
        CSVFormat.DEFAULT.withTrim()
      )
    }.bracket({ csvParser -> IO { csvParser.close() } }) { csvParser ->
      IO {
        val records: List<CSVRecord> = csvParser.records
        Option.monad().binding {
          val key =
            records.getColumnIndex(keyColumn).bind()
          val column =
            records.getColumnIndex(compareColumn).bind()
          records.tail().map { benchmarkFromCSV(it, key, column) }
        }.fix()
      }.flatMap {
        it.fold({ IO.raiseError(BenchmarkInconsistencyError) },
          { IO { it } })
      }
    }

}