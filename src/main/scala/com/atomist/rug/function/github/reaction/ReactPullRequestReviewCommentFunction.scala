package com.atomist.rug.function.github.reaction

import java.net.URL

import com.atomist.rug.function.github.GitHubFunction
import com.atomist.rug.function.github.reaction.GitHubReactions.Reaction
import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, JsonBodyOption, StringBodyOption}
import com.atomist.source.git.github.domain.ReactionContent
import com.typesafe.scalalogging.LazyLogging

/**
  * Reacts to a GitHub pull request review comment
  */
class ReactPullRequestReviewCommentFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "react-github-pull-request-review-comment", description = "Reacts to a GitHub pull request review comment",
    tags = Array(new Tag(name = "github"), new Tag(name = "pull requests"), new Tag(name = "comments"), new Tag(name = "reactions")))
  def invoke(@Parameter(name = "reaction") reaction: String,
             @Parameter(name = "pullRequestId") pullRequestId: Int,
             @Parameter(name = "commentId") commentId: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "owner") owner: String,
             @Parameter(name = "apiUrl") apiUrl: String,
             @Secret(name = "user_token", path = "github://user_token?scopes=repo") token: String): FunctionResponse = {
    try {
      val ghs = gitHubServices(token, apiUrl)
      val react = ghs.createPullRequestReviewCommentReaction(repo, owner, commentId, ReactionContent.withName(reaction))
      val response = Reaction(react.id,  new URL(react.user.url), react.content.toString)
      FunctionResponse(Status.Success, Some(s"Successfully created pull request review comment reaction for `$commentId`"), None, JsonBodyOption(response))
    } catch {
      case e: Exception =>
        val msg = s"Failed to add reaction to pull request review comment reaction for `$commentId`"
        logger.error(msg, e)
        FunctionResponse(Status.Failure, Some(msg), None, StringBodyOption(e.getMessage))
    }
  }
}
