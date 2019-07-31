package com.fortysevendeg.hood.github

import arrow.core.Option
import com.fortysevendeg.hood.models.GhStatusState
import java.net.URI

data class GhInfo(val owner: String, val repo: String, val token: String)

data class GhStatus(
  val state: GhStatusState,
  val targetUrl: Option<URI>,
  val description: String,
  val context: String = "benchmark-ci/hood"
)

data class GhUser(val login: String)

data class GhComment(val id: Long, val user: GhUser, val body: String)

data class GhFileSha(val sha: String)

data class GhCreateCommit(val message: String, val content: String, val branch: String)

data class GhUpdateCommit(
  val message: String,
  val content: String,
  val sha: String,
  val branch: String
)
