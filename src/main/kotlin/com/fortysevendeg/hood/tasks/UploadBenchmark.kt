package com.fortysevendeg.hood.tasks

import arrow.core.toOption
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.monad.monad
import com.fortysevendeg.hood.BenchmarkReader
import com.fortysevendeg.hood.GhCreateCommit
import com.fortysevendeg.hood.GhInfo
import com.fortysevendeg.hood.GhUpdateCommit
import com.fortysevendeg.hood.github.GithubCommitIntegration
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UploadBenchmark : DefaultTask() {

  @get:Input
  var benchmarkFile: File = project.objects.property(File::class.java).getOrElse(File("master.csv"))
  @get:Input
  var benchmarkDestinationFromProjectRoot: String =
    project.objects.property(String::class.java).getOrElse("master.csv")
  @get:Input
  var commitMessage: String =
    project.objects.property(String::class.java).getOrElse("Upload benchmark")
  @get:Input
  var token: String? = project.objects.property(String::class.java).orNull

  private val ciName: String = "travis"

  @TaskAction
  fun uploadBenchmark(): Unit = IO.monad().binding {
    val branch: String = IO { System.getenv("TRAVIS_BRANCH") }.bind()

    val slug = IO { System.getenv("TRAVIS_REPO_SLUG").split('/') }.bind()
    if (slug.size < 2)
      IO.raiseError<Unit>(GradleException("Error reading env var: TRAVIS_REPO_SLUG")).bind()
    val owner: String = slug.first()
    val repo: String = slug.last()

    val token: String =
      token.toOption()
        .fold({ IO.raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }
        .bind()

    val info = GhInfo(owner, repo, token)

    val fileSha =
      GithubCommitIntegration.getFileSha(info, branch, benchmarkDestinationFromProjectRoot).bind()

    val content = BenchmarkReader.readFileToBase64(benchmarkFile).bind()

    fileSha.fold({
      val createCommit = GhCreateCommit(commitMessage, content, branch)
      GithubCommitIntegration.createFileCommit(
        info,
        benchmarkDestinationFromProjectRoot,
        createCommit
      )
    }, {
      val updateCommit =
        GhUpdateCommit(commitMessage, content, it.sha, branch)
      GithubCommitIntegration.updateFileCommit(
        info,
        benchmarkDestinationFromProjectRoot,
        updateCommit
      )
    }).bind()

  }.fix().unsafeRunSync()

}
