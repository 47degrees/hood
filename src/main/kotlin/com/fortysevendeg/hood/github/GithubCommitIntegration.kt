package com.fortysevendeg.hood.github

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.effects.IO
import com.fortysevendeg.hood.github.GithubCommon.buildRequest
import com.fortysevendeg.hood.github.GithubCommon.client
import com.fortysevendeg.hood.github.GithubCommon.raiseError
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson
import org.http4k.format.Jackson.auto
import org.http4k.format.Jackson.json

object GithubCommitIntegration {

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