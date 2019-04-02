package com.fortysevendeg.hood.tasks

import arrow.core.toOption
import com.fortysevendeg.hood.Comparator
import com.fortysevendeg.hood.JsonSupport
import com.fortysevendeg.hood.OutputFile
import com.fortysevendeg.hood.prettyPrintResult
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
  var threshold: Double? = project.objects.property(Double::class.java).orNull

  @TaskAction
  fun compareBenchmark(): Unit =
    Comparator.compareBenchmarks(
      previousBenchmarkPath,
      currentBenchmarkPath,
      keyColumnName,
      compareColumnName,
      thresholdColumnName,
      threshold.toOption()
    ).flatMap {
      println(it.prettyPrintResult())
      val allJson = JsonSupport.areAllJson(currentBenchmarkPath.plus(previousBenchmarkPath))
      OutputFile.sendOutputToFile(outputToFile, allJson, outputPath, it, outputFormat)
    }.unsafeRunSync()

}
