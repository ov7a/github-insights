package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import ru.ov7a.github.insights.domain.PullRequestsBatch
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.fetcher.graphql.GraphQLPullRequestsClient

class PullRequestsClient(
    clientBuilder: (HttpClientConfig<*>.() -> Unit) -> HttpClient = ::HttpClient
) {
    private val client: HttpClient = clientBuilder {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }

    private val graphQLPullRequestsClient = GraphQLPullRequestsClient(client)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    suspend fun fetchAll(repositoryId: RepositoryId, authorizationHeader: String): Flow<PullRequestsBatch> =
        graphQLPullRequestsClient.fetchAll(repositoryId, authorizationHeader)

}
