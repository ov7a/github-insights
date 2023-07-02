package ru.ov7a.github.insights.fetcher.graphql

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.ov7a.github.insights.domain.PullRequestsBatch
import ru.ov7a.github.insights.domain.RepositoryId

class GraphQLPullRequestsClient(
    private val client: HttpClient
) {
    private suspend fun fetch(
        repositoryId: RepositoryId,
        cursor: String?,
        authorizationHeader: String
    ): GraphQLPullRequestsResponse {
        return client.post(GRAPH_QL_URL) {
            contentType(ContentType.Application.Json)
            setBody(createPullRequestsQuery(repositoryId, cursor))
            headers {
                append(HttpHeaders.Authorization, authorizationHeader)
            }
        }.body()
    }

    private fun GraphQLPullRequestsResponse.toBatch() = PullRequestsBatch(
        totalCount = this.data.repository.pullRequests.totalCount,
        pullRequests = this.data.repository.pullRequests.nodes
    )

    suspend fun fetchAll(repositoryId: RepositoryId, authorizationHeader: String): Flow<PullRequestsBatch> =
        flow {
            var cursor: String? = null
            do {
                val response = fetch(repositoryId, cursor, authorizationHeader)
                emit(response.toBatch())
                cursor = response.data.repository.pullRequests.pageInfo.startCursor
            } while (response.data.repository.pullRequests.pageInfo.hasPreviousPage)
        }

    private companion object {
        const val GRAPH_QL_URL = "https://api.github.com/graphql"
    }
}
