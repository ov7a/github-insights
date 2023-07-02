package ru.ov7a.github.insights.fetcher.graphql

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.features.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.name
import kotlin.js.json
import kotlin.test.Test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import ru.ov7a.github.insights.Endpoint
import ru.ov7a.github.insights.domain.PullRequest
import ru.ov7a.github.insights.domain.PullRequestState
import ru.ov7a.github.insights.domain.PullRequestsBatch
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.fetcher.withMockEngine
import ru.ov7a.github.insights.loadResource
import ru.ov7a.github.insights.mockResponse
import ru.ov7a.github.insights.response
import ru.ov7a.github.insights.runTest

class GraphQLPullRequestsClientTests {

    private companion object {
        const val url = "https://api.github.com/graphql"
        const val authHeader = "Basic someBase64String"

        val defaultRequestHeaders = mapOf(
            HttpHeaders.Authorization to listOf(authHeader),
            HttpHeaders.Accept to listOf(ContentType.Application.Json.toString()),
            HttpHeaders.AcceptCharset to listOf(Charsets.UTF_8.name),
        )

        val defaultResponseHeaders = mapOf(
            HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
        )

        private fun graphQlQuery(resource: String) = JSON.stringify(
            json("query" to loadResource(resource))
        )
    }

    @Test
    fun should_fetch_single_page_properly() = runTest {
        val result = withMockEngine(
            mockResponse(
                Endpoint(
                    url = url,
                    method = HttpMethod.Post,
                    headers = defaultRequestHeaders
                ),
                graphQlQuery("requests/graphql/example.graphql"),
                response(
                    content = loadResource("responses/graphql/example_page.json"),
                    headers = defaultResponseHeaders
                )
            )
        ) {
            fetchAll(RepositoryId("octocat", "Hello-World"), authHeader).toList()
        }

        result shouldBe listOf(
            PullRequestsBatch(
                totalCount = 370,
                pullRequests = listOf(
                    PullRequest(
                        url = "https://github.com/octocat/Hello-World/pull/1046",
                        state = PullRequestState.MERGED,
                        createdAt = Instant.fromEpochMilliseconds(1296068472_000),
                        updatedAt = Instant.fromEpochMilliseconds(1296068532_000),
                        closedAt = Instant.fromEpochMilliseconds(1296068592_000),
                        mergedAt = Instant.fromEpochMilliseconds(1296068652_000)
                    ),
                    PullRequest(
                        url = "fake_url",
                        state = PullRequestState.OPEN,
                        createdAt = Instant.fromEpochMilliseconds(1332878745_000)
                    ),
                    PullRequest(
                        url = "fake_url2",
                        state = PullRequestState.CLOSED,
                        createdAt = Instant.fromEpochMilliseconds(1296068472_000),
                        updatedAt = Instant.fromEpochMilliseconds(1296068532_000),
                        closedAt = Instant.fromEpochMilliseconds(1296068592_000),
                    ),
                ),
            )
        )
    }

    @Test
    fun should_properly_fetch_several_pages() = runTest {
        fun responseMock(dataFile: String) = mockResponse(
            Endpoint(
                url = url,
                method = HttpMethod.Post,
                headers = defaultRequestHeaders
            ),
            graphQlQuery("requests/graphql/$dataFile.graphql"),
            response(
                content = loadResource("responses/graphql/$dataFile.json"),
                headers = defaultResponseHeaders
            )
        )

        val result = withMockEngine(
            responseMock(dataFile = "page1"),
            responseMock(dataFile = "page2"),
            responseMock(dataFile = "page3")
        ) {
            fetchAll(RepositoryId("octocat", "Hello-World"), authHeader).toList()
        }

        result shouldHaveSize 3
        result.flatMap { it.pullRequests } shouldHaveSize 7
    }

    @Test
    fun fetch_all_should_rethrow_exception() = runTest {
        val exception = shouldThrow<ClientRequestException> {
            val resultFlow = withMockEngine(
                mockResponse(
                    Endpoint(
                        url = url,
                        method = HttpMethod.Post,
                        headers = defaultRequestHeaders
                    ),
                    graphQlQuery("requests/graphql/non-existing.graphql"),
                    response(
                        statusCode = HttpStatusCode.NotFound,
                        content = "",
                        headers = defaultResponseHeaders
                    )
                )
            ) {
                fetchAll(RepositoryId("octocat", "non-existing"), authHeader)
            }
            resultFlow.collect()
        }
        exception.message shouldContain "404 Not Found"
    }

}
