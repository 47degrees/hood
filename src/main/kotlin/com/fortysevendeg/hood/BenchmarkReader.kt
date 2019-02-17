package com.fortysevendeg.hood

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
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader

object BenchmarkReader {

  private fun benchmarkFromCSV(row: CSVRecord, key: Int, column: Int): Benchmark = Benchmark(
    row[key].substringAfterLast('.'),
    row[column].toDouble()
  )

  private fun List<CSVRecord>.getColumnIndex(columnName: String): Option<Int> =
    this.firstOrNone().map { it.indexOf(columnName) }

  private fun <A, B> Map<A, List<Pair<A, List<B>>>>.mapValuesToSecond(): Map<A, List<B>> =
    this.mapValues { it.value.flatMap { it.second } }

  private fun ListK<Pair<String, ListK<Benchmark>>>.groupByBenchmarkKey(): Map<String, List<Benchmark>> =
    this.groupBy { it.first }.mapValuesToSecond()

  private fun readCSV(
    file: FileReader,
    keyColumn: String,
    compareColumn: String
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
          val key =
            records.getColumnIndex(keyColumn).bind()
          val column =
            records.getColumnIndex(compareColumn).bind()
          records.tail().map { benchmarkFromCSV(it, key, column) }
        }.fix()
      }.flatMap {
        it.fold({ IO.raiseError<ListK<Benchmark>>(BenchmarkInconsistencyError) },
          { IO { it.k() } })
      }
    }

  fun readFilesToBenchmark(
    keyColumn: String,
    compareColumn: String,
    vararg files: File
  ): IO<Map<String, List<Benchmark>>> =
    files.toList().traverse(
      IO.applicative()
    ) { file ->
      IO { FileReader(file) }.flatMap {
        readCSV(
          it,
          keyColumn,
          compareColumn
        ).map { file.nameWithoutExtension to it }
      }
    }.fix().map { it.fix().groupByBenchmarkKey() }

  fun readFiletoBase64(file: File): IO<String> {
    fun toBase64(text: String): IO<String> = TODO()
    return IO { FileReader(file) }.bracket({ IO { it.close() } }, { toBase64(it.readText()) })
  }

}