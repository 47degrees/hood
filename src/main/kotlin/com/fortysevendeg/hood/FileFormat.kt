package com.fortysevendeg.hood

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.effects.IO
import org.gradle.api.GradleException
import java.io.File

enum class FileFormat {
  MD, JSON;

  override fun toString(): String {
    return super.toString().toLowerCase()
  }

  companion object {

    private fun toFileFormat(str: String): Option<FileFormat> = when {
      str.toLowerCase() == "md"   -> FileFormat.MD.some()
      str.toLowerCase() == "json" -> FileFormat.JSON.some()
      else                        -> None
    }

    fun getFileFormat(file: File) = toFileFormat(file.extension.toLowerCase())

    fun toFileFormatOrRaise(str: String) = FileFormat.toFileFormat(str).fold({
      IO.raiseError<FileFormat>(GradleException("Unknown format to file output"))
    }) { IO { it } }

  }
}