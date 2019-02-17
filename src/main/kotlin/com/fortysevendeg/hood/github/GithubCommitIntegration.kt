package com.fortysevendeg.hood.github

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.effects.IO
import com.fortysevendeg.hood.GhCreateCommit
import com.fortysevendeg.hood.GhFileSha
import com.fortysevendeg.hood.GhInfo
import com.fortysevendeg.hood.GhUpdateCommit
import org.gradle.api.GradleException
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.format.Jackson
import org.http4k.format.Jackson.auto
import org.http4k.format.Jackson.json

object GithubCommitIntegration {

  private val client: DualSyncAsyncHttpHandler = OkHttp()

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

  fun getFileSha(info: GhInfo, branch: String, path: String): IO<Option<GhFileSha>> {

    val body = Jackson {
      obj("ref" to string(branch))
    }

    val request = buildRequest(
      Method.GET,
      info,
      "contents/$path"
    ).with(Body.json().toLens() of body)

    return IO { client(request) }.map {
      when (it.status) {
        Status.OK -> Jackson.asA(it.bodyString(), GhFileSha::class).some()
        else      -> None
      }
    }
  }

  fun createFileCommit(info: GhInfo, path: String, commit: GhCreateCommit): IO<Unit> {

    val messageLens = Body.auto<GhCreateCommit>().toLens()

    val request = buildRequest(
      Method.PUT,
      info,
      "contents/$path"
    )
    val requestWithBody = messageLens.inject(commit, request)

    return IO { client(requestWithBody) }.flatMap {
      if (it.status == Status.CREATED)
        IO.unit
      else
        raiseError("Error creating the file ${it.status}")
    }
  }

  fun updateFileCommit(info: GhInfo, path: String, commit: GhUpdateCommit): IO<Unit> {
    val messageLens = Body.auto<GhUpdateCommit>().toLens()

    val request = buildRequest(
      Method.PUT,
      info,
      "contents/$path"
    )
    val requestWithBody = messageLens.inject(commit, request)

    return IO { client(requestWithBody) }.flatMap {
      if (it.status == Status.OK)
        IO.unit
      else
        raiseError("Error updating the file ${it.status}")
    }
  }

}