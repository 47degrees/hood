package com.fortysevendeg.hood

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.File

open class CompareBenchmark : DefaultTask() {

  @get:Input
  var previousBenchmarkPath: File = project.objects.property<File>().getOrElse(File("master.csv"))
  @get:Input
  var currentBenchmarkPath: List<File> = project.objects.listProperty<File>().getOrElse(listOf())
  @get:Input
  var keyColumnName: String = project.objects.property<String>().getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property<String>().getOrElse("Score")
  @get:Input
  var threshold: Int = project.objects.property<Int>().getOrElse(50)

  @TaskAction
  fun compareBenchmark() {
    val result: List<BenchmarkComparison> = Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      threshold,
      keyColumnName,
      compareColumnName
    ).unsafeRunSync()
    println(result.prettyPrintResult())
  }

}
