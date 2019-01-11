package com.adrianrafo.hood

import arrow.core.*
import java.io.FileReader
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

object HeliosBenchmarkReader {

  private fun benchmarkFromCSV(row: CSVRecord): Benchmark = Benchmark(
    row[0],
    row[1],
    row[2].toInt(),
    row[3].toInt(),
    row[4].toDouble(),
    row[5].toDouble(),
    row[6]
  )

  private fun read(path: String): Option<HeliosBenchmark> {
    val csvParser = CSVParser(
      FileReader(path),
      CSVFormat.DEFAULT.withSkipHeaderRecord().withTrim()
    )

    val benchmarkList = Try {
      val benchmarks: List<Benchmark> = csvParser.records.map { benchmarkFromCSV(it) }
      csvParser.close()
      benchmarks
    }.getOrElse {
      println("Error reading the csv: ${it.message}")
      listOf()
    }

    val maybeDecoding = benchmarkList.find { it.name.contains("decoding") }.toOption()
    val maybeParsing = benchmarkList.find { it.name.contains("parsing") }.toOption()

    return maybeDecoding.flatMap { decoding ->
      maybeParsing.map { parsing ->
        HeliosBenchmark(
          decoding,
          parsing
        )
      }
    }
  }

//  fun readCSVFile(path: String): IO<List<Benchmark>> =
//    IO.monad().binding {
//      val file = IO { BufferedReader(FileReader(path)) }.bind()
//      val lines = IO { file.readLines().drop(1) }.bind()
//      val result: Either<Throwable, ListK<Option<Benchmark>>> =
//        lines.k().traverse(Either.applicative()) { parseLine(it) }.fix()
//      result.map { it.mapFilter { it }.list }
//    }
//      .fix()
//      .attempt()
//      .flatMap { it.flatten().fold({ ex -> IO.raiseError<List<Benchmark>>(ex) }, { list -> IO.just(list) }) }

}