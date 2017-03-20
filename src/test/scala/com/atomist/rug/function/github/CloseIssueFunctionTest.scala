package com.atomist.rug.function.github

import com.atomist.rug.function.github.GitHubSearchIssues.GitHubIssue
import com.atomist.rug.function.github.TestCredentials.Token
import com.atomist.rug.spi.Handlers.Status
import com.atomist.util.JsonUtils

class CloseIssueFunctionTest extends GitHubFunctionTest(Token) {

  it should "close issue" in {
    val tempRepo = newPopulatedTemporaryRepo()
    val issue = createIssue(tempRepo, "test issue", "Issue body")
    issue.addAssignees(gitHub.getUser("alankstewart"))

    val f = new CloseIssueFunction
    val response = f.invoke(issue.getNumber, tempRepo.getName, tempRepo.getOwnerName, Token)
    response.status shouldBe Status.Success

    val f2 = new SearchIssuesFunction
    val response2 = f2.invoke(null, tempRepo.getName, tempRepo.getOwnerName, Token)
    response2.status shouldBe Status.Success
    val body = response2.body
    body shouldBe defined
    body.get.str shouldBe defined
    val issues = JsonUtils.fromJson[Seq[GitHubIssue]](body.get.str.get)
    issues should have size 1
    issues.head.state shouldEqual "CLOSED"
  }
}