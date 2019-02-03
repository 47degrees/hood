package com.adrianrafo.hood

import org.http4k.client.AsyncHttpClient
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request

object GithubIntegration {

  val client: AsyncHttpClient = OkHttp()

  //https://developer.github.com/v3/pulls/comments/
  private fun getPreviousComment(): () -> Unit = {
    val request = Request(Method.GET, "https://api.github.com/repos/:owner/:repo/pulls/comments")

    client(request) {
      println(it)
    }
    //TODO filter by author
  }

  private fun createComment(): () -> Unit = {
    val request =
      Request(Method.POST, "https://api.github.com/repos/:owner/:repo/pulls/:number/comments")

    client(request) {
      println(it)
    }
    //TODO check in reply to param to create a top comment
  }

  //TODO edit or delete and create a new one?
  private fun editComment(): () -> Unit = {
    val request =
      Request(Method.PATCH, "https://api.github.com/repos/:owner/:repo/pulls/comments/:comment_id")

    client(request) {
      println(it)
    }
  }

  private fun deleteComment(): () -> Unit = {
    val request =
      Request(Method.DELETE, "https://api.github.com/repos/:owner/:repo/pulls/comments/:comment_id")

    client(request) {
      println(it)
    }
  }

  fun setCommentResult(result: List<BenchmarkResult>) {
    getPreviousComment()
    createComment()
    editComment()
    deleteComment()
  }


  fun setStatus(): () -> Unit = {
    val request =
      Request(Method.POST, "/repos/:owner/:repo/statuses/:sha")

    client(request) {
      println(it)
    }
  }

}