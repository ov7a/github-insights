package ru.ov7a.github.insights.calculation

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import ru.ov7a.github.insights.domain.PullRequest
import ru.ov7a.github.insights.domain.PullRequestState
import ru.ov7a.github.insights.domain.Statistic
import ru.ov7a.github.insights.runTest

@ExperimentalTime
@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
class CalculateTest {

    @Test
    fun should_calculate_result() = runTest {
        val pulls = flowOf(
            PullRequest(
                "don't care",
                PullRequestState.OPEN,
                createdAt = Instant.fromEpochMilliseconds(1000)
            ),
            PullRequest(
                "don't care",
                PullRequestState.CLOSED,
                createdAt = Instant.fromEpochMilliseconds(2000),
                mergedAt = Instant.fromEpochMilliseconds(3500)
            ),
            PullRequest(
                "don't care",
                PullRequestState.CLOSED,
                createdAt = Instant.fromEpochMilliseconds(4000),
                closedAt = Instant.fromEpochMilliseconds(4500)
            ),
            PullRequest(
                "don't care",
                PullRequestState.CLOSED,
                createdAt = Instant.fromEpochMilliseconds(3000),
                closedAt = Instant.fromEpochMilliseconds(4000),
                mergedAt = Instant.fromEpochMilliseconds(5000)
            )
        )
        //Durations: 5000, 1500, 500, 1000
        val result = calculateDurationStats(pulls, now = Instant.fromEpochMilliseconds(6000))
        result shouldBe listOf(
            Statistic("0th percentile (minimum)", Duration.milliseconds(500)),
            Statistic("10th percentile", Duration.milliseconds(650)),
            Statistic("20th percentile", Duration.milliseconds(800)),
            Statistic("30th percentile", Duration.milliseconds(950)),
            Statistic("40th percentile", Duration.milliseconds(1100)),
            Statistic("50th percentile (mean)", Duration.milliseconds(1250)),
            Statistic("60th percentile", Duration.milliseconds(1400)),
            Statistic("70th percentile", Duration.milliseconds(1850)),
            Statistic("80th percentile", Duration.milliseconds(2900)),
            Statistic("90th percentile", Duration.milliseconds(3950)),
            Statistic("95th percentile", Duration.milliseconds(4475)),
            Statistic("99th percentile", Duration.milliseconds(4895)),
            Statistic("100th percentile (maximum)", Duration.milliseconds(5000)),
            Statistic("Average", Duration.milliseconds(2000)),
        )
    }

    @Test
    fun can_return_null_for_empty_array() = runTest {
        val result = calculateDurationStats(flowOf())
        result shouldBe null
    }
}
