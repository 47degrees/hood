package com.fortysevendeg.hood.tasks

import arrow.core.toOption
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import com.fortysevendeg.hood.OutputFile
import com.fortysevendeg.hood.github.GhCreateCommit
import com.fortysevendeg.hood.github.GhInfo
import com.fortysevendeg.hood.github.GhUpdateCommit
import com.fortysevendeg.hood.github.GithubCommitIntegration
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UploadBenchmark : DefaultTask() {

  @get:InputFile
  var benchmarkFile: File =
    project.objects.fileProperty().asFile.getOrElse(File("benchmarks/master_benchmark.csv"))
  @get:Input
  var benchmarkDestinationFromProjectRoot: String =
    project.objects.property(String::class.java).getOrElse("benchmarks/master_benchmark.csv")
  @get:Input
  var commitMessage: String =
    project.objects.property(String::class.java).getOrElse("Upload benchmark")
  @get:Input
  var token: String? = project.objects.property(String::class.java).orNull

  @TaskAction
  fun uploadBenchmark(): Unit = fx {
    val branch: String = !IO { System.getenv("TRAVIS_BRANCH") }

    val slug = !IO { System.getenv("TRAVIS_REPO_SLUG").split('/') }
    if (slug.size < 2) !raiseError<Unit>(GradleException("Error reading env var: TRAVIS_REPO_SLUG"))
    val owner: String = slug.first()
    val repo: String = slug.last()

    val (token: String) =
      token.toOption()
        .fold({ raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }

    val info = GhInfo(owner, repo, token)

    val fileSha =
      !GithubCommitIntegration.getFileSha(info, branch, benchmarkDestinationFromProjectRoot)

    val content = !OutputFile.readFileToBase64(benchmarkFile)

    fileSha.fold({
      val createCommit = GhCreateCommit(commitMessage, content, branch)
      !GithubCommitIntegration.createFileCommit(
        info,
        benchmarkDestinationFromProjectRoot,
        createCommit
      )
    }, {
      val updateCommit =
        GhUpdateCommit(commitMessage, content, it.sha, branch)
      !GithubCommitIntegration.updateFileCommit(
        info,
        benchmarkDestinationFromProjectRoot,
        updateCommit
      )
    })

  }.fix().unsafeRunSync()

}
