package ru.ov7a.github.insights.calculation

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.datetime.Clock
import ru.ov7a.github.insights.domain.PullRequest
import ru.ov7a.github.insights.domain.PullRequestState
import ru.ov7a.github.insights.domain.PullRequestsBatch
import ru.ov7a.github.insights.runTest

class ProgressReporterTests {
    private fun reporter() = object : ru.ov7a.github.insights.calculation.ProgressReporter() {
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
