package ru.ov7a.github.insights.calculation

import getAndCalculateStats
import io.kotest.assertions.withClue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.ItemType
import ru.ov7a.github.insights.domain.ProgressReporter
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.domain.Statistic
import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.ClientsProvider
import ru.ov7a.github.insights.fetcher.createClientWithMocks
import ru.ov7a.github.insights.fetcher.graphql.pulls.PullRequestsClient
import ru.ov7a.github.insights.loadResource
import ru.ov7a.github.insights.runTest

@OptIn(ExperimentalTime::class)
class MainTests {
    private fun response(dataFile: String) = ru.ov7a.github.insights.response(
        content = loadResource("responses/graphql/$dataFile.json"),
        headers = mapOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
    )

    private val authHeader = "Basic someAuth"

    @Test
    fun should_collect_stats() = runTest {
        val client = clientsProvider(
            response("pulls/page1"),
            response("pulls/page2"),
            response("pulls/page3"),
        )

        val reporter = ProgressReporter()

        val result = getAndCalculateStats(
            FetchParameters(
                ItemType.PULL,
                RepositoryId("octocat", "Hello-World"),
                authHeader,
            ),
            reporter,
            client
        )
        withClue(result) {
            result.isSuccess shouldBe true
        }
        val stats = result.getOrNull()
        stats?.size shouldBe 14 + 1
        stats?.last() shouldBe Statistic("Total count", 7)
    }

    @Test
    fun should_catch_error() = runTest {
        val client = clientsProvider(
            ru.ov7a.github.insights.response(content = "", statusCode = HttpStatusCode.NotFound)
        )

        val reporter = ProgressReporter()

        val result = getAndCalculateStats(
            FetchParameters(
                ItemType.PULL,
                RepositoryId("octocat", "Hello-World"),
                authHeader,
            ),
            reporter,
            client
        )
        withClue(result) {
            result.isFailure shouldBe true
        }
        result.exceptionOrNull() should beInstanceOf<ClientRequestException>()
    }

    @Test
    fun should_process_empty_result() = runTest {
        val client = clientsProvider(
            response("pulls/page_empty")
        )
        val reporter = ProgressReporter()

        val result = getAndCalculateStats(
            FetchParameters(
                ItemType.PULL,
                RepositoryId("octocat", "Hello-World"),
                authHeader,
            ),
            reporter,
            client
        )

        withClue(result) {
            result.isSuccess shouldBe true
        }
        result.getOrNull() shouldBe null
    }

    private fun clientsProvider(
        vararg handlers: MockRequestHandler
    ): ClientsProvider = object : ClientsProvider {
        override val pullRequests: Client = createClientWithMocks({ PullRequestsClient(it) }, *handlers)
        override val issues: Client
            get() = throw Exception("Should not be called")
    }
}