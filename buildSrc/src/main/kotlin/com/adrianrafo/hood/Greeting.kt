package com.adrianrafo.hood

import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.property

open class Greeting : DefaultTask() {

  @get:Input
  var message = project.objects.property<String>().getOrElse("World")

  @TaskAction
  fun greeting(): Unit =
    println("Hello, ${message}")

}
