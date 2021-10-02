package ru.ov7a.pull_requests.calculation

import getAndCalculateStats
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.ktor.client.features.*
import io.ktor.http.*
import ru.ov7a.pull_requests.domain.RepositoryId
import ru.ov7a.pull_requests.domain.Statistic
import ru.ov7a.pull_requests.fetcher.createClientWithMocks
import ru.ov7a.pull_requests.loadResource
import ru.ov7a.pull_requests.response
import ru.ov7a.pull_requests.runTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MainTests {
    private fun response(dataFile: String) = response(
        content = loadResource("responses/graphql/$dataFile.json"),
        headers = mapOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
    )

    private val authHeader = "Basic someAuth"

    @Test
    fun should_collect_stats() = runTest {
        val client = createClientWithMocks(
            response("page1"),
            response("page2"),
            response("page3"),
        )

        val reporter = ProgressReporter()

        val result = getAndCalculateStats(
            client,
            RepositoryId("octocat", "Hello-World"),
            authHeader,
            reporter
        )

        result.isSuccess shouldBe true
        val stats = result.getOrNull()
        stats?.size shouldBe 14 + 1
        stats?.last() shouldBe Statistic("Total count", 7)
    }

    @Test
    fun should_catch_error() = runTest {
        val client = createClientWithMocks(
            response(content = "", statusCode = HttpStatusCode.NotFound)
        )

        val reporter = ProgressReporter()

        val result = getAndCalculateStats(
            client,
            RepositoryId("octocat", "Hello-World"),
            authHeader,
            reporter
        )
        result.isFailure shouldBe true
        result.exceptionOrNull() should beInstanceOf<ClientRequestException>()
    }

    @Test
    fun should_process_empty_result() = runTest {
        val client = createClientWithMocks(
            response("page_empty")
        )
        val reporter = ProgressReporter()

        val result = getAndCalculateStats(
            client,
            RepositoryId("octocat", "Hello-World"),
            authHeader,
            reporter
        )

        result.isSuccess shouldBe true
        result.getOrNull() shouldBe null
    }
}