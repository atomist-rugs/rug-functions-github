package com.atomist.rug.function.github.pullrequest

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.atomist.source.git.github.GitHubServices
import com.typesafe.scalalogging.LazyLogging

/**
  * Merge a pull request.
  */
class MergePullRequestFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "merge-github-pull-request", description = "Merges a pull request",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues"), new Tag(name = "pr")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking mergePullRequest with number '$number', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    try {
      val ghs = GitHubServices(token, apiUrl)
      ghs.getPullRequest(repo, owner, number).map(pr => {
        ghs.mergePullRequest(repo, owner, pr.number, pr.title, "Merged pull request")
        FunctionResponse(Status.Success, Some(s"Successfully merged pull request `$number`"), None)
      }).getOrElse(FunctionResponse(Status.Failure, Some(s"Failed to find pull request `$number`"), None, None))
    } catch {
      case e: Exception =>
        val msg = s"Failed to merge pull request `$number`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
