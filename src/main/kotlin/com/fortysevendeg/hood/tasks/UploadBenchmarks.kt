package com.fortysevendeg.hood.tasks

import arrow.core.extensions.option.applicative.map2
import arrow.core.getOrElse
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

open class UploadBenchmarks : DefaultTask() {

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
  @get:Input
  var repositoryOwner: String? = project.objects.property(String::class.java).orNull
  @get:Input
  var repositoryName: String? = project.objects.property(String::class.java).orNull
  @get:Input
  var branch: String =
    project.objects.property(String::class.java).getOrElse("master")

  private fun upload(info: GhInfo, branch: String, benchmarkFile: File): IO<Unit> = fx {

    val parent = if (uploadDirectory.endsWith('/')) uploadDirectory else "$uploadDirectory/"
    val destination = parent + benchmarkFile.name
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
  fun uploadBenchmarks(): Unit =
    repositoryOwner.toOption().map2(repositoryName.toOption()) { (owner, repo) ->
      fx {
        val (ghToken: String) = token.toOption().getOrRaiseError { GradleException("Error getting Github token") }

        val info = GhInfo(owner, repo, ghToken)

        !benchmarkFiles.traverse_(IO.applicative()) { upload(info, branch, it) }
      }.fix()
    }.getOrElse {
      IO.raiseError(GradleException("Missing one of the following parameters: 'repositoryOwner', 'repositoryName'"))
    }.unsafeRunSync()

}
