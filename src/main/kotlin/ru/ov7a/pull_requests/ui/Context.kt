package ru.ov7a.pull_requests.ui

import ru.ov7a.pull_requests.fetcher.PullRequestsClient

data class Context(
    val repositoryId: RepositoryIdContext = RepositoryIdContext(),
    val authorization: AuthorizationContext = AuthorizationContext(),
    val presentation: PresentationContext = PresentationContext(),
    val pullRequestsClient: PullRequestsClient = PullRequestsClient()
)