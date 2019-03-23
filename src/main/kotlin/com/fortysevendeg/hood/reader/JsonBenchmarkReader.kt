package com.fortysevendeg.hood.reader

import arrow.data.ListK
import arrow.data.extensions.list.traverse.traverse
import arrow.data.fix
import arrow.data.k
import arrow.effects.IO
import arrow.effects.extensions.io.applicative.applicative
import arrow.effects.fix
import com.fasterxml.jackson.module.kotlin.readValue
import com.fortysevendeg.hood.JsonBenchmark
import com.fortysevendeg.hood.JsonSupport
import java.io.File
import java.io.FileReader

object JsonBenchmarkReader : BenchmarkReader {

  private fun readJson(reader: FileReader): IO<ListK<JsonBenchmark>> =
    IO { reader.readText() }.flatMap {
      IO { JsonSupport.mapper.readValue<List<JsonBenchmark>>(it).k() }
    }

  fun readFilesToBenchmark(
    vararg files: File
  ): IO<Map<String, List<JsonBenchmark>>> =
    files.toList().traverse(
      IO.applicative()
    ) { file ->
      IO { FileReader(file) }.bracket({ IO { it.close() } }) { fileReader ->
        readJson(fileReader).map { file.nameWithoutExtension to it }
      }
    }.fix().map { it.fix().groupByKey() }

}