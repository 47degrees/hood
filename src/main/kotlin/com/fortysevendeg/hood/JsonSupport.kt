package com.fortysevendeg.hood

import arrow.core.some
import arrow.data.extensions.list.foldable.forAll
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

object JsonSupport {

  private val printer = DefaultPrettyPrinter().let { prt ->
    val indenter = DefaultIndenter("  ", DefaultIndenter.SYS_LF)
    prt.indentObjectsWith(indenter)
    prt.indentArraysWith(indenter)
    prt
  }
  val mapper: ObjectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL).setDefaultPrettyPrinter(printer)

  fun List<File>.areAllJson() =
    this.forAll { FileFormat.getFileFormat(it) == FileFormat.JSON.some() }
}