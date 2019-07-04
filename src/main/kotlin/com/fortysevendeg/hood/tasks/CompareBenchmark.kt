package com.fortysevendeg.hood.tasks

import com.fortysevendeg.hood.HoodComparison
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

open class CompareBenchmark : DefaultTask() {

  //Benchmarks paths
  @get:InputFile
  var previousBenchmarkPath: File =
    project.objects.fileProperty().asFile.getOrElse(File("master.csv"))
  @get:InputFiles
  var currentBenchmarkPath: List<File> =
    project.objects.listProperty(File::class.java).getOrElse(emptyList())

  //CSV columns
  @get:Input
  var keyColumnName: String = project.objects.property(String::class.java).getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property(String::class.java).getOrElse("Score")
  @get:Input
  var thresholdColumnName: String =
    project.objects.property(String::class.java).getOrElse("Score Error (99.9%)")

  //Output
  @get:Input
  var outputToFile: Boolean = project.objects.property(Boolean::class.java).getOrElse(false)
  @get:Input
  var outputPath: String =
    project.objects.property(String::class.java).getOrElse("./hood/comparison")
  @get:Input
  var outputFormat: String =
    project.objects.property(String::class.java).getOrElse("md")

  //Extra
  @get:Input
  @Optional
  var generalThreshold: Double? = project.objects.property(Double::class.java).orNull
  @get:Input
  @Optional
  var benchmarkThreshold: Map<String, Double>? =
    project.objects.mapProperty(String::class.java, Double::class.java).orNull
  @get:Input
  @Optional
  var include: String? = project.objects.property(String::class.java).orNull
  @get:Input
  @Optional
  var exclude: String? = project.objects.property(String::class.java).orNull

  @TaskAction
  fun compareBenchmark(): Unit =
    HoodComparison.compare(
      previousBenchmarkPath,
      currentBenchmarkPath,
      keyColumnName,
      compareColumnName,
      thresholdColumnName,
      outputToFile,
      outputPath,
      outputFormat,
      generalThreshold,
      benchmarkThreshold,
      include,
      exclude
    ).unsafeRunSync()

}
