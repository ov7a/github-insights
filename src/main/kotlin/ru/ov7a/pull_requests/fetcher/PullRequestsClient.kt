package ru.ov7a.pull_requests.fetcher

import io.ktor.client.*
import io.ktor.client.features.cookies.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import ru.ov7a.pull_requests.domain.PullRequestsBatch
import ru.ov7a.pull_requests.domain.RepositoryId
import ru.ov7a.pull_requests.fetcher.graphql.GraphQLPullRequestsClient
import ru.ov7a.pull_requests.fetcher.rest.RestPullRequestsClient

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

    private val restPullRequestsClient = RestPullRequestsClient(client)
    private val graphQLPullRequestsClient = GraphQLPullRequestsClient(client)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    suspend fun fetchAll(repositoryId: RepositoryId, authorizationHeader: String? = null): Flow<PullRequestsBatch> =
        if (authorizationHeader != null) {
            graphQLPullRequestsClient.fetchAll(repositoryId, authorizationHeader)
        } else {
            restPullRequestsClient.fetchAll(repositoryId)
        }

}