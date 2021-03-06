package com.fortysevendeg.hood.tasks

import arrow.core.Option
import arrow.core.extensions.option.applicative.applicative
import arrow.core.fix
import arrow.core.getOrElse
import arrow.core.toOption
import com.fortysevendeg.hood.HoodComparison
import com.fortysevendeg.hood.github.GhInfo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import java.net.URI

open class CompareBenchmarksCI : DefaultTask() {

  //Benchmarks paths
  @get:InputFile
  var previousBenchmarkPath: File =
    project.objects.fileProperty().asFile.getOrElse(File("master.csv"))
  @get:InputFiles
  var currentBenchmarkPath: List<File> =
    project.objects.listProperty(File::class.java).getOrElse(emptyList())

  //Field names
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

  //CI
  @get:Input
  @Optional
  var token: String? = project.objects.property(String::class.java).orNull
  @get:Input
  @Optional
  var repositoryOwner: String? = project.objects.property(String::class.java).orNull
  @get:Input
  @Optional
  var repositoryName: String? = project.objects.property(String::class.java).orNull
  @get:Input
  @Optional
  var pullRequestSha: String? = project.objects.property(String::class.java).orNull
  @get:Input
  @Optional
  var pullRequestNumber: Int? = project.objects.property(Int::class.java).orNull
  @get:Input
  @Optional
  var statusTargetUrl: URI? = project.objects.property(URI::class.java).orNull

  @TaskAction
  fun compareBenchmarksCI() =
    Option.applicative().map(
      token.toOption(),
      repositoryOwner.toOption(),
      repositoryName.toOption(),
      pullRequestSha.toOption(),
      pullRequestNumber.toOption()
    ) { (token, owner, name, sha, number) ->
      HoodComparison.compareCI(
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
        exclude,
        GhInfo(owner, name, token),
        sha,
        number,
        statusTargetUrl.toOption()
      )
    }.fix().getOrElse {

      println(
        """|Missing one of the following parameters: 'repositoryOwner', 'repositoryName', 'pullRequestSha', 'pullRequestNumber'.
           |Comparison without CI features will be executed.""".trimMargin()
      )

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
      )
    }.unsafeRunSync()

}
