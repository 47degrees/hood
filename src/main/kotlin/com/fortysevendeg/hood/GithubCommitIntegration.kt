package com.fortysevendeg.hood

import arrow.core.Option
import arrow.effects.IO
import arrow.syntax.collections.firstOption
import org.gradle.api.GradleException
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.format.Jackson
import org.http4k.format.Jackson.json

object GithubCommitIntegration {

  val client: DualSyncAsyncHttpHandler = OkHttp()

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

  private fun raiseError(error: String): IO<Unit> =
    IO.raiseError(GradleException("Error accessing Github Commit Api: $error"))

  fun createCommit(info: GhInfo, pull: Int, file: String): IO<Boolean> {

    val body = Jackson {
      obj("body" to string(file))
    }

    val request = buildRequest(
      Method.POST,
      info,
      "issues/$pull/comments"
    ).with(Body.json().toLens() of body)

    return IO { client(request) }.map { it.status.code == 201 }
  }


}