package com.fortysevendeg.hood

import arrow.core.Option
import arrow.core.toOption
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.monad.monad
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class UploadBenchmark : DefaultTask() {

  @get:Input
  var BenchmarkPath: String = project.objects.property(String::class.java).getOrElse("master.csv")
  @get:Input
  var token: Option<String> = project.objects.property(String::class.java).orNull.toOption()

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
      token.fold({ IO.raiseError<String>(GradleException("Error getting Github token")) }) { IO { it } }
        .bind()

    val info = GhInfo(owner, repo, token)


  }.fix().unsafeRunSync()

}
