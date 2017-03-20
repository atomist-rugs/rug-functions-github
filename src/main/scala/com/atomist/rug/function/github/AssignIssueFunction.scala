package com.atomist.rug.function.github

import com.atomist.rug.spi.Handlers.Status
import com.atomist.rug.spi.annotation.{Parameter, RugFunction, Secret, Tag}
import com.atomist.rug.spi.{AnnotatedRugFunction, FunctionResponse, StringBodyOption}
import com.typesafe.scalalogging.LazyLogging
import org.kohsuke.github.GitHub

import scala.util.{Failure, Success, Try}

/**
  * Assigns an issue to a user.
  */
class AssignIssueFunction
  extends AnnotatedRugFunction
    with LazyLogging
    with GitHubFunction {

  @RugFunction(name = "assign-github-issue", description = "Assigns an GitHub issue",
    tags = Array(new Tag(name = "github"), new Tag(name = "issues")))
  def invoke(@Parameter(name = "issue") number: Int,
             @Parameter(name = "repo") repo: String,
             @Parameter(name = "assignee") assignee: String,
             @Parameter(name = "owner") owner: String,
             @Secret(name = "user_token", path = "user/github/token?scope=repo") token: String): FunctionResponse = {

    logger.info(s"Invoking assignIssue with number '$number', assignee '$assignee', owner '$owner', repo '$repo' and token '${safeToken(token)}'")

    Try {
      val gitHub = GitHub.connectUsingOAuth(token)
      val repository = gitHub.getOrganization(owner).getRepository(repo)
      val issue = repository.getIssue(number)
      issue.addAssignees(gitHub.getUser(assignee))
    } match {
      case Success(_) => FunctionResponse(Status.Success, Option(s"Successfully assigned issue `#$number` in `$owner/$repo` to `$assignee`"), None)
      case Failure(e) => FunctionResponse(Status.Failure, Some(s"Failed to assign issue `#$number` in `$owner/$repo` to `$assignee`"), None, StringBodyOption(e.getMessage))
    }
  }
}