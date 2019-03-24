package com.fortysevendeg.hood

import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import com.fortysevendeg.hood.models.BenchmarkComparison
import com.fortysevendeg.hood.models.OutputFileFormat
import org.gradle.api.GradleException
import java.io.File
import java.io.FileWriter
import java.util.Base64

object OutputFile {

  private fun createFile(file: File): IO<Boolean> =
    IO(file.parentFile::mkdirs).map { file.createNewFile() }

  private fun writeOutputFile(
    path: String,
    result: List<BenchmarkComparison>,
    format: OutputFileFormat
  ): IO<Unit> = fx {

    val file = !effect { File("$path.$format") }
    val exists = !effect { file.exists() }

    if (exists || !createFile(file))
      !IO { FileWriter(file) }.bracket({ IO(it::close) }) { writer ->
        IO { writer.write(result.prettyPrintResult(format)) }
          .flatMap { IO(writer::flush) }
      }
    else !raiseError<Unit>(GradleException("Cannot create the file"))

  }.fix()

  fun sendOutputToFile(
    outputToFile: Boolean,
    allJson: Boolean,
    path: String,
    result: List<BenchmarkComparison>,
    outputFormat: String
  ): IO<Unit> = fx {
    if (outputToFile)
      !OutputFileFormat.toFileFormatOrRaise(outputFormat).flatMap {
        if (it == OutputFileFormat.JSON && allJson || it != OutputFileFormat.JSON)
          writeOutputFile(path, result, it)
        else raiseError<Unit>(
          GradleException("Wrong output format selected, all the benchmarks must to be Json in order to print one")
        )
      }
    else !unit()
  }.fix()

  fun readFileToBase64(file: File): IO<String> =
    IO { Base64.getEncoder().encodeToString(file.readBytes()) }

}