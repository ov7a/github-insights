package ru.ov7a.github.insights.fetcher

import kotlinx.coroutines.flow.Flow
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.input.DataBatch
import ru.ov7a.github.insights.fetcher.graphql.issues.IssuesClient
import ru.ov7a.github.insights.fetcher.graphql.pulls.PullRequestsClient

interface Client {
    suspend fun fetchAll(fetchParameters: FetchParameters): Flow<DataBatch>
}

interface ClientsProvider {
    val pullRequests: Client
    val issues: Client
}

object Clients : ClientsProvider {
    private val jsonClient = JsonClient()
    override val pullRequests: Client = PullRequestsClient(jsonClient)
    override val issues: Client = IssuesClient(jsonClient)
}
