package ru.ov7a.github.insights.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.datetime.Clock
import ru.ov7a.github.insights.domain.input.DataBatch
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.runTest

class ProgressReporterTests {
    private fun reporter() = object : ProgressReporter() {
        var lastReported: Double? = null

        override suspend fun report(value: Double) {
            lastReported = value
        }
    }

    private fun pullRequestsBatch(total: Int, size: Int) = DataBatch(
        totalCount = total,
        data = List(size) {
            IssueLike(url = "url", createdAt = Clock.System.now(), labels = emptyList(), comments = 0, reactions = 0)
        }
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
