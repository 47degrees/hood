package com.adrianrafo.hood

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

open class GreetPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    group = Settings.group
    tasks{
      register("greet", Greeting::class)
    }
  }

  object Settings {
    val group = "com.adrianrafo.kotlin_dsl_hello"
  }
}