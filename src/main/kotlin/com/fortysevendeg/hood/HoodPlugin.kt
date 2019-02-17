package com.fortysevendeg.hood

import org.gradle.api.Plugin
import org.gradle.api.Project

open class HoodPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    group = Settings.group
    tasks.register("compareBenchmark", CompareBenchmark::class.java)
    tasks.register("compareBenchmarkCI", CompareBenchmarkCI::class.java)
    tasks.register("uploadBenchmark", UploadBenchmark::class.java)
  }

  object Settings {
    const val group = "com.fortysevendeg.hood"
  }
}