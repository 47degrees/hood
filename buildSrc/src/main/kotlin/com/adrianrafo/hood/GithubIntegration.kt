package com.adrianrafo.hood

import arrow.effects.IO
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.format.Jackson

object GithubIntegration {

  val client: DualSyncAsyncHttpHandler = OkHttp()

  val commentIntro = "***Hood benchmark comparison:***"
  val commonHeaders: String = TODO()

  private fun getPreviousComment(info: GhInfo) {
    val request = Request(
      Method.GET,
      "https://api.github.com/repos/${info.owner}/${info.repo}/issues/${info.pull}/comments"
    )

    IO { client(request) }.map { Jackson.run { it.bodyString().asJsonObject() } }
    // TODO decode to ghComment
    //body.startsWith(commentIntro)
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

  fun setCommentResult(info: GhInfo, result: List<BenchmarkResult>) :IO<Boolean> = TODO()

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