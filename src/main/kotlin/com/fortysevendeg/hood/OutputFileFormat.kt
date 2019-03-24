package com.fortysevendeg.hood

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.effects.IO
import org.gradle.api.GradleException
import java.io.File

enum class OutputFileFormat {
  MD, JSON;

  override fun toString(): String = super.toString().toLowerCase()

  companion object {

    private fun toFileFormat(str: String): Option<OutputFileFormat> = when {
      str.toLowerCase() == MD.toString()   -> OutputFileFormat.MD.some()
      str.toLowerCase() == JSON.toString() -> OutputFileFormat.JSON.some()
      else                                 -> None
    }

    fun getFileFormat(file: File): Option<OutputFileFormat> =
      toFileFormat(file.extension.toLowerCase())

    fun toFileFormatOrRaise(str: String): IO<OutputFileFormat> =
      OutputFileFormat.toFileFormat(str).fold({
        IO.raiseError(GradleException("Unknown format to file output"))
      }) { IO { it } }

  }
}