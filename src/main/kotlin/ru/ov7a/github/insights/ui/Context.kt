package ru.ov7a.github.insights.ui

import ru.ov7a.github.insights.fetcher.PullRequestsClient

data class Context(
    val repositoryId: RepositoryIdContext = RepositoryIdContext(),
    val authorization: AuthorizationContext = AuthorizationContext(),
    val presentation: PresentationContext = PresentationContext(),
    val pullRequestsClient: PullRequestsClient = PullRequestsClient()
)
