package ru.ov7a.pull_requests.calculation

import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import ru.ov7a.pull_requests.domain.PullRequest
import ru.ov7a.pull_requests.domain.PullRequestState
import ru.ov7a.pull_requests.domain.PullRequestsBatch
import ru.ov7a.pull_requests.runTest
import kotlin.test.Test

class ProgressReporterTests {
    private fun reporter() = object : ProgressReporter() {
        var lastReported: Double? = null

        override suspend fun report(value: Double) {
            lastReported = value
        }
    }

    private fun pullRequestsBatch(total: Int, size: Int) = PullRequestsBatch(
        totalCount = total,
        pullRequests = List(size) { PullRequest("url", PullRequestState.OPEN, Clock.System.now()) }
    )

    @Test
    fun should_report_fraction() = runTest {
        reporter().apply {
            consume(pullRequestsBatch(50, 10))
            lastReported shouldBe 0.2
        }
    }

    @Test
    fun should_report_zero_as_all() = runTest {
        reporter().apply {
            consume(pullRequestsBatch(0, 0))
            lastReported shouldBe 1.0
        }
    }

    @Test
    fun should_store_actual_count() = runTest {
        reporter().apply {
            consume(pullRequestsBatch(50, 0))
            count shouldBe 0
            consume(pullRequestsBatch(50, 10))
            count shouldBe 10
            consume(pullRequestsBatch(50, 30))
            count shouldBe 40
        }
    }
}