package com.fortysevendeg.hood

import arrow.data.extensions.list.foldable.forAll
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

object JsonSupport {

  //2 spaces printer
  private val printer = DefaultPrettyPrinter().let { prt ->
    val indenter = DefaultIndenter("  ", DefaultIndenter.SYS_LF)
    prt.indentObjectsWith(indenter)
    prt.indentArraysWith(indenter)
    prt
  }

  //Json with indentation and ignoring null values
  val mapper: ObjectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL).setDefaultPrettyPrinter(printer)

  fun areAllJson(list: List<File>): Boolean =
    list.forAll { file ->
      OutputFileFormat.getFileFormat(file).exists { it == OutputFileFormat.JSON }
    }
}