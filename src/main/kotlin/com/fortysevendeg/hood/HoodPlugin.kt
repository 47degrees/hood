package com.fortysevendeg.hood

import com.fortysevendeg.hood.tasks.CompareBenchmarks
import com.fortysevendeg.hood.tasks.CompareBenchmarksCI
import com.fortysevendeg.hood.tasks.UploadBenchmarks
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
open class HoodPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    group = Settings.group
    tasks.register("compareBenchmarks", CompareBenchmarks::class.java)
    tasks.register("compareBenchmarksCI", CompareBenchmarksCI::class.java)
    tasks.register("uploadBenchmarks", UploadBenchmarks::class.java)
  }

  object Settings {
    const val group: String = "com.fortysevendeg.hood"
  }
}