package com.fortysevendeg.hood.github

import arrow.core.Option
import arrow.effects.IO
import arrow.syntax.collections.firstOption
import com.fortysevendeg.hood.*
import org.gradle.api.GradleException
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.format.Jackson
import org.http4k.format.Jackson.json

object GithubCommentIntegration {

  private val client: DualSyncAsyncHttpHandler = OkHttp()

  private const val commentIntro: String = "***Hood benchmark comparison:***"

  private fun buildRequest(method: Method, info: GhInfo, url: String): Request {
    val commonHeaders: List<Pair<String, String>> = listOf(
      Pair("Accept", "application/vnd.github.v3+json"),
      Pair("Authorization", "token ${info.token}"),
      Pair("Content-Type", "application/json")
    )

    return Request(
      method,
      "https://api.github.com/repos/${info.owner}/${info.repo}/$url"
    ).headers(commonHeaders)
  }

  fun raiseError(error: String): IO<Unit> =
    IO.raiseError(GradleException("Error accessing Github Comment Api: $error"))

  fun getPreviousCommentId(info: GhInfo, ciName: String, pull: Int): IO<Option<Long>> {
    val request = buildRequest(
      Method.GET,
      info,
      "issues/$pull/comments"
    )

    return IO { client(request) }.map { Jackson.asA(it.bodyString(), Array<GhComment>::class) }
      .map { list ->
        list.filter { it.user.login.contains(ciName) && it.body.startsWith(commentIntro) }
          .firstOption().map { it.id }
      }
  }

  fun createComment(info: GhInfo, pull: Int, results: List<BenchmarkComparison>): IO<Boolean> {

    val content = "$commentIntro\n${results.prettyPrintResult()}"

    val body = Jackson {
      obj("body" to string(content))
    }

    val request = buildRequest(
      Method.POST,
      info,
      "issues/$pull/comments"
    ).with(Body.json().toLens() of body)

    return IO { client(request) }.map { it.status == Status.CREATED }
  }

  fun deleteComment(info: GhInfo, id: Long): IO<Boolean> {
    val request = buildRequest(
      Method.DELETE,
      info,
      "issues/comments/$id"
    )
    return IO { client(request) }.map { it.status == Status.NO_CONTENT }
  }

  fun setStatus(info: GhInfo, commitSha: String, status: GhStatus): IO<Unit> {

    val body = Jackson {
      obj(
        "value" to string(status.state.value),
        "description" to string(status.description),
        "context" to string(status.context)
      )
    }

    val request =
      buildRequest(
        Method.POST,
        info,
        "statuses/$commitSha"
      ).with(Body.json().toLens() of body)

    return IO { client(request) }.flatMap {
      if (it.status == Status.CREATED)
        IO.unit
      else
        raiseError("Error creating the status ${it.status}")
    }
  }

}