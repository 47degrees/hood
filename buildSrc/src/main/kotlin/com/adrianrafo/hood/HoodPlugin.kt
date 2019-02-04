package com.adrianrafo.hood

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

open class HoodPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    group = Settings.group
    tasks{
      register("compareBenchmark", CompareBenchmark::class)
      register("compareBenchmarkCI", CompareBenchmarkCI::class)
    }
  }

  object Settings {
    val group = "com.adrianrafo.hood"
  }
}