package com.adrianrafo.hood

import arrow.core.Option
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.monad.monad
import arrow.syntax.collections.firstOption
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.format.Jackson
import org.http4k.format.Jackson.json

object GithubIntegration {

  val client: DualSyncAsyncHttpHandler = OkHttp()

  val commentIntro = "***Hood benchmark comparison:***"
  val commonHeaders: String = TODO()

  private fun getPreviousComment(info: GhInfo, ciName: String): IO<Option<GhComment>> {
    val request = Request(
      Method.GET,
      "https://api.github.com/repos/${info.owner}/${info.repo}/issues/${info.pull}/comments"
    )

    return IO { client(request) }.map { Jackson.asA(it.bodyString(), Array<GhComment>::class) }
      .map { list ->
        list.filter { it.user.login.contains(ciName) && it.body.startsWith(commentIntro) }
          .firstOption()
      }
  }

  private fun createComment(info: GhInfo, result: List<BenchmarkResult>): IO<Boolean> {

    val content = "$commentIntro\n${result.prettyPrintResult()}"

    val body = Jackson {
      obj("body" to string(content))
    }

    val request =
      Request(
        Method.POST,
        "https://api.github.com/repos/${info.owner}/${info.repo}/issues/${info.pull}/comments"
      ).with(Body.json().toLens() of body)

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

  fun setCommentResult(info: GhInfo, result: List<BenchmarkResult>): IO<Boolean> =
    IO.monad().binding {
      val previousComment = getPreviousComment(info, "travis").bind()
      val cleanResult = previousComment.fold { IO { true } }{ deleteComment(info, it.id) }.bind()
      if (cleanResult)
        createComment(info, result).bind()
      else
        IO { false }.bind()
    }.fix()

  fun setStatus(info: GhInfo, commitSha: String, status: GhStatus): IO<Boolean> {

    val body = Jackson {
      obj(
        "value" to string(status.state.value),
        "description" to string(status.description),
        "context" to string(status.context)
      )
    }

    val request =
      Request(
        Method.POST,
        "https://api.github.com//repos/${info.owner}/${info.repo}/statuses/$commitSha"
      ).with(Body.json().toLens() of body)

    return IO { client(request) }.map { it.status.code == 201 }
  }

}