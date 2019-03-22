package com.fortysevendeg.hood

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonSupport {

  private val printer = DefaultPrettyPrinter().let { prt ->
    val indenter = DefaultIndenter("  ", DefaultIndenter.SYS_LF)
    prt.indentObjectsWith(indenter)
    prt.indentArraysWith(indenter)
    prt
  }
  val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL).setDefaultPrettyPrinter(printer)
}