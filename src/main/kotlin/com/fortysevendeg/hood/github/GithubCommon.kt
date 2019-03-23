package com.fortysevendeg.hood.github

import arrow.effects.IO
import org.gradle.api.GradleException
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request

object GithubCommon {

  val client: DualSyncAsyncHttpHandler = OkHttp()

  fun buildRequest(method: Method, info: GhInfo, url: String): Request {
    val commonHeaders: List<Pair<String, String>> = listOf(
      "Accept" to "application/vnd.github.v3+json",
      "Authorization" to "token ${info.token}",
      "Content-Type" to "application/json"
    )

    return Request(
      method,
      "https://api.github.com/repos/${info.owner}/${info.repo}/$url"
    ).headers(commonHeaders)
  }

  fun raiseError(error: String): IO<Unit> =
    IO.raiseError(GradleException("Error accessing Github Commit Api: $error"))

}