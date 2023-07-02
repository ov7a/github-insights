package ru.ov7a.github.insights.fetcher

import kotlinx.coroutines.flow.Flow
import ru.ov7a.github.insights.domain.DataBatch
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.fetcher.graphql.issues.IssuesClient
import ru.ov7a.github.insights.fetcher.graphql.pulls.PullRequestsClient

interface Client {
    suspend fun fetchAll(
        repositoryId: RepositoryId,
        authorizationHeader: String
    ): Flow<DataBatch>
}

object Clients {
    private val jsonClient = JsonClient()
    val pullRequests: Client = PullRequestsClient(jsonClient)
    val issues: Client = IssuesClient(jsonClient)
}
