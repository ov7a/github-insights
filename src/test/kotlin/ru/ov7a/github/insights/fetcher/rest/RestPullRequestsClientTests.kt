package ru.ov7a.github.insights.fetcher.rest

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.features.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.name
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

class RestPullRequestsClientTests {

    private companion object {
        const val contentTypeHeader = "application/vnd.github.v3+json"
        val defaultRequestHeaders = mapOf(
            HttpHeaders.Accept to listOf(contentTypeHeader, ContentType.Application.Json.toString()),
            HttpHeaders.AcceptCharset to listOf(Charsets.UTF_8.name),
        )

        val exampleLinkHeader =
            """<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=2>; rel="prev", 
            |<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=4>; rel="next", 
            |<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=8>; rel="last", 
            |<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=1>; rel="first"""".trimMargin()

        val exampleLinkHeaderLastPage =
            """<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=7>; rel="prev", 
            |<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=8>; rel="last", 
            |<https://api.github.com/repositories/2325298/pulls?state=all&per_page=100&page=1>; rel="first"""".trimMargin()

        val defaultResponseHeaders = mapOf(
            HttpHeaders.ContentType to listOf(contentTypeHeader),
            HttpHeaders.Link to listOf(exampleLinkHeader),
        )
    }

    @Test
    fun should_fetch_single_page_properly() = runTest {
        val result = withMockEngine(
            mockResponse(
                Endpoint(
                    "https://api.github.com/repos/octocat/Hello-World/pulls?state=all&per_page=100&page=1",
                    headers = defaultRequestHeaders
                ),
                response(
                    content = loadResource("responses/rest/example_page.json"),
                    headers = defaultResponseHeaders.plus(HttpHeaders.Link to listOf(exampleLinkHeaderLastPage))
                )
            )
        ) {
            fetchAll(RepositoryId("octocat", "Hello-World")).toList()
        }

        result shouldBe listOf(
            PullRequestsBatch(
                totalCount = 800, //approximate
                pullRequests = listOf(
                    PullRequest(
                        url = "https://github.com/octocat/Hello-World/pull/1347",
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
                )
            )
        )
    }

    @Test
    fun should_properly_fetch_several_pages() = runTest {
        fun responseMock(page: Int, dataFile: String, lastPage: Boolean = false) = mockResponse(
            Endpoint(
                "https://api.github.com/repos/octocat/Hello-World/pulls?state=all&per_page=100&page=$page",
                headers = defaultRequestHeaders
            ),
            response(
                content = loadResource("responses/rest/$dataFile"),
                headers = if (lastPage) {
                    defaultResponseHeaders.plus(HttpHeaders.Link to listOf(exampleLinkHeaderLastPage))
                } else {
                    defaultResponseHeaders
                }
            )
        )

        val result = withMockEngine(
            responseMock(page = 1, dataFile = "page1.json"),
            responseMock(page = 2, dataFile = "page2.json"),
            responseMock(page = 3, dataFile = "page3.json", lastPage = true)
        ) {
            fetchAll(RepositoryId("octocat", "Hello-World")).toList()
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
                        "https://api.github.com/repos/octocat/non-existing/pulls?state=all&per_page=100&page=1",
                        headers = defaultRequestHeaders
                    ), response(
                        statusCode = HttpStatusCode.NotFound,
                        content = "",
                        headers = defaultResponseHeaders
                    )
                )
            ) {
                fetchAll(RepositoryId("octocat", "non-existing"))
            }
            resultFlow.collect()
        }
        exception.message shouldContain "404 Not Found"
    }

    @Test
    fun fetchAll_can_process_empty_array() = runTest {
        val result = withMockEngine(
            mockResponse(
                Endpoint(
                    "https://api.github.com/repos/octocat/Hello-World/pulls?state=all&per_page=100&page=1",
                    headers = defaultRequestHeaders
                ),
                response(
                    content = "[]",
                    headers = defaultResponseHeaders.plus(HttpHeaders.Link to listOf(exampleLinkHeaderLastPage))
                )
            )
        ) {
            fetchAll(RepositoryId("octocat", "Hello-World")).toList()
        }

        result shouldBe listOf(PullRequestsBatch(totalCount = 800, pullRequests = emptyList()))
    }

}
