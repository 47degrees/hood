package com.adrianrafo.hood

import arrow.core.Option
import arrow.effects.IO
import arrow.instances.list.foldable.forAll
import arrow.syntax.collections.firstOption
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.format.Jackson

object GithubIntegration {

  val client: DualSyncAsyncHttpHandler = OkHttp()

  val commentIntro = "***Hood benchmark comparison:***"
  val commonHeaders: String = TODO()

  private fun getPreviousComment(ciName: String, info: GhInfo): IO<Option<GhComment>> {
    val request = Request(
      Method.GET,
      "https://api.github.com/repos/${info.owner}/${info.repo}/issues/${info.pull}/comments"
    )

    return IO { client(request) }.map { Jackson.asA(it.bodyString(), Array<GhComment>::class) }
      .map { list -> list.filter { it.user.login.contains(ciName) && it.body.startsWith(commentIntro) }.firstOption() }
  }

  private fun createComment(info: GhInfo): IO<Boolean> {
    val request =
      Request(
        Method.POST,
        "https://api.github.com/repos/${info.owner}/${info.repo}/issues/${info.pull}/comments"
      )

    //TODO set request body

    return IO { client(request) }.map { it.status.code == 201 }
  }

  private fun deleteComment(info: GhInfo, id: Long): IO<Boolean> {
    val request =
      Request(
        Method.DELETE,
        "https://api.github.com/repos/${info.owner}/${info.repo}/issues/comments/$id"
      )

    return IO { client(request) }.map { it.status.code == 204 }
  }

  fun setCommentResult(info: GhInfo, result: List<BenchmarkResult>): IO<Boolean> = TODO()

  fun setStatus(info: GhInfo, commitSha: String, status: GhStatus): IO<Boolean> {
    val request =
      Request(
        Method.POST,
        "https://api.github.com//repos/${info.owner}/${info.repo}/statuses/$commitSha"
      )
    //TODO set body
    return IO { client(request) }.map { it.status.code == 201 }
  }

}