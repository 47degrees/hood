package com.fortysevendeg.hood

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CompareBenchmark : DefaultTask() {

  @get:InputFile
  var previousBenchmarkPath: File = project.objects.property(File::class.java).getOrElse(File("master.csv"))
  @get:InputFiles
  var currentBenchmarkPath: List<File> = project.objects.listProperty(File::class.java).getOrElse(emptyList())
  @get:Input
  var keyColumnName: String = project.objects.property(String::class.java).getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property(String::class.java).getOrElse("Score")
  @get:Input
  var threshold: Int = project.objects.property(Int::class.java).getOrElse(50)

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
