package com.fortysevendeg.hood.jsonsupport

import arrow.effects.IO
import com.fasterxml.jackson.module.kotlin.readValue
import com.fortysevendeg.hood.JsonBenchmark
import com.fortysevendeg.hood.JsonSupport
import com.fortysevendeg.hood.jsonsupport.jsonSample.benchmarkJson
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class JsonBenchmarkDecodeSpec : StringSpec({

  "encode and decode json benchmarks" {

    val benchmarkE = IO {
      JsonSupport.mapper.readValue<JsonBenchmark>(benchmarkJson)
    }.attempt().unsafeRunSync()
    benchmarkE.shouldBeRight()

    val benchmark = benchmarkE.toOption().orNull()
    JsonSupport.mapper.writeValueAsString(benchmark) shouldBe benchmarkJson

  }

})