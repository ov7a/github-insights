package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import ru.ov7a.github.insights.domain.PullRequestsBatch
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.fetcher.graphql.GraphQLPullRequestsClient

class PullRequestsClient(
    clientBuilder: (HttpClientConfig<*>.() -> Unit) -> HttpClient = ::HttpClient
) {
    private val client: HttpClient = clientBuilder {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }

    private val graphQLPullRequestsClient = GraphQLPullRequestsClient(client)

    suspend fun fetchAll(repositoryId: RepositoryId, authorizationHeader: String): Flow<PullRequestsBatch> =
        graphQLPullRequestsClient.fetchAll(repositoryId, authorizationHeader)
}
