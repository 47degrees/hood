package com.fortysevendeg.hood.tasks

import arrow.core.toOption
import arrow.data.extensions.list.foldable.traverse_
import arrow.effects.IO
import arrow.effects.extensions.io.applicative.applicative
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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UploadBenchmark : DefaultTask() {

  @get:InputFiles
  var benchmarkFiles: List<File> =
    project.objects.listProperty(File::class.java).getOrElse(emptyList())
  @get:Input
  var uploadDirectory: String =
    project.objects.property(String::class.java).getOrElse("benchmarks")
  @get:Input
  var commitMessage: String =
    project.objects.property(String::class.java).getOrElse("Upload benchmark")
  @get:Input
  var token: String? = project.objects.property(String::class.java).orNull

  private fun upload(info: GhInfo, branch: String, benchmarkFile: File): IO<Unit> = fx {

    val destination = uploadDirectory + benchmarkFile.name
    val fileSha = !GithubCommitIntegration.getFileSha(info, branch, destination)

    val content = !OutputFile.readFileToBase64(benchmarkFile)

    fileSha.fold({
      val createCommit = GhCreateCommit(commitMessage, content, branch)
      !GithubCommitIntegration.createFileCommit(
        info,
        destination,
        createCommit
      )
    }, {
      val updateCommit =
        GhUpdateCommit(commitMessage, content, it.sha, branch)
      !GithubCommitIntegration.updateFileCommit(
        info,
        destination,
        updateCommit
      )
    })

  }

  @TaskAction
  fun uploadBenchmark(): Unit = fx {
    val branch: String = !IO { System.getenv("TRAVIS_BRANCH") }

    val slug = !IO { System.getenv("TRAVIS_REPO_SLUG").split('/') }
    if (slug.size < 2) !raiseError<Unit>(GradleException("Error reading env var: TRAVIS_REPO_SLUG"))
    val owner: String = slug.first()
    val repo: String = slug.last()

    val (ghToken: String) =
      token.toOption()
        .fold({ raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }

    val info = GhInfo(owner, repo, ghToken)

    !benchmarkFiles.traverse_(IO.applicative()) {
      upload(info, branch, it)
    }

  }.fix().unsafeRunSync()

}
