package com.fortysevendeg.hood.tasks

import com.fortysevendeg.hood.Comparator
import com.fortysevendeg.hood.OutputFile
import com.fortysevendeg.hood.syntax.prettyPrintResult
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CompareBenchmark : DefaultTask() {

  @get:InputFile
  var previousBenchmarkPath: File =
    project.objects.fileProperty().asFile.getOrElse(File("master.csv"))
  @get:InputFiles
  var currentBenchmarkPath: List<File> =
    project.objects.listProperty(File::class.java).getOrElse(emptyList())
  @get:Input
  var keyColumnName: String = project.objects.property(String::class.java).getOrElse("Benchmark")
  @get:Input
  var compareColumnName: String = project.objects.property(String::class.java).getOrElse("Score")
  @get:Input
  var threshold: Int = project.objects.property(Int::class.java).getOrElse(50)
  @get:Input
  var outputToFile: Boolean = project.objects.property(Boolean::class.java).getOrElse(false)
  @get:Input
  var outputPath: String =
    project.objects.property(String::class.java).getOrElse("./hood/comparison")
  @get:Input
  var outputFormat: String =
    project.objects.property(String::class.java).getOrElse("md")

  @TaskAction
  fun compareBenchmark() =
    Comparator.compareCsv(
      previousBenchmarkPath,
      currentBenchmarkPath,
      threshold,
      keyColumnName,
      compareColumnName
    ).flatMap {
      println(it.prettyPrintResult())
      OutputFile.sendOutputToFile(outputToFile, outputPath, it, outputFormat)
    }.unsafeRunSync()

}
