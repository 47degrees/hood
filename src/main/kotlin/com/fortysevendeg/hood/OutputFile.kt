package com.fortysevendeg.hood

import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.monad.monad
import com.fortysevendeg.hood.syntax.prettyPrintResult
import com.fortysevendeg.hood.syntax.toFileFormat
import org.gradle.api.GradleException
import java.io.File
import java.io.FileWriter

object OutputFile {

  private fun createFile(file: File): IO<Boolean> =
    IO { file.parentFile.mkdirs() }.map { file.createNewFile() }

  private fun writeOutputFile(
    path: String,
    result: List<BenchmarkComparison>,
    format: FileFormat
  ): IO<Unit> = IO.monad().binding {

    val file = IO { File("$path.$format") }.bind()
    val exists = IO { file.exists() }.bind()

    if (exists || createFile(file).bind()) {

      val writer = IO { FileWriter(file) }.bind()

      IO { writer.write(result.prettyPrintResult(format)) }.bind()
      IO { writer.flush() }.bind()
      IO { writer.close() }.bind()

    } else IO.raiseError<Unit>(GradleException("Cannot create the file")).bind()

  }.fix()

  fun sendOutputToFile(
    outputToFile: Boolean,
    path: String,
    result: List<BenchmarkComparison>,
    outputFormat: String
  ): IO<Unit> = IO.monad().binding {
    if (outputToFile) {

      outputFormat.toFileFormat().fold({
        IO.raiseError<Unit>(GradleException("Unknown format to file output")).bind()
      }, { writeOutputFile(path, result, it).bind() })

    } else IO.unit.bind()
  }.fix()

}