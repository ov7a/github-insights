package ru.ov7a.github.insights.ui

import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.Clients

data class Context(
    val repositoryId: RepositoryIdContext = RepositoryIdContext(),
    val authorization: AuthorizationContext = AuthorizationContext(),
    val presentation: PresentationContext = PresentationContext(),
    val pullRequestsClient: Client = Clients.pullRequests,
    val issuesClient: Client = Clients.issues,
)
