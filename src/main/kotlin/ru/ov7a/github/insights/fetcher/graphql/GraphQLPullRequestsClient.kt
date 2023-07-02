package ru.ov7a.github.insights.fetcher.graphql

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
        val response: GraphQLPullRequestsResponse = client.post(
            scheme = URLProtocol.HTTPS.name,
            host = HOST,
            path = PATH,
        ) {
            body = createPullRequestsQuery(repositoryId, cursor)
            headers {
                contentType(ContentType.Application.Json)
                append(HttpHeaders.Authorization, authorizationHeader)
            }
        }
        return response
    }

    private fun GraphQLPullRequestsResponse.toBatch() = PullRequestsBatch(
        totalCount = this.data.repository.pullRequests.totalCount,
        pullRequests = this.data.repository.pullRequests.nodes
    )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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
        const val HOST = "api.github.com"
        const val PATH = "/graphql"
    }
}
