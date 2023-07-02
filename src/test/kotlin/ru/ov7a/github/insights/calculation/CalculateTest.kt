package ru.ov7a.github.insights.calculation

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import ru.ov7a.github.insights.domain.IssueLike
import ru.ov7a.github.insights.domain.Statistic
import ru.ov7a.github.insights.runTest

@ExperimentalTime
class CalculateTest {

    @Test
    fun should_calculate_result() = runTest {
        val pulls = flowOf(
            create(
                createdAt = 1000
            ),
            create(
                createdAt = 2000,
                closedAt = 3500
            ),
            create(
                createdAt = 4000,
                closedAt = 4500
            ),
            create(
                createdAt = 3000,
                closedAt = 4000
            )
        )
        //Durations: 5000, 1500, 500, 1000
        val result = calculateDurationStats(pulls, now = Instant.fromEpochMilliseconds(6000))
        result shouldBe listOf(
            Statistic("0th percentile (minimum)", 500.milliseconds),
            Statistic("10th percentile", 650.milliseconds),
            Statistic("20th percentile", 800.milliseconds),
            Statistic("30th percentile", 950.milliseconds),
            Statistic("40th percentile", 1100.milliseconds),
            Statistic("50th percentile (mean)", 1250.milliseconds),
            Statistic("60th percentile", 1400.milliseconds),
            Statistic("70th percentile", 1850.milliseconds),
            Statistic("80th percentile", 2900.milliseconds),
            Statistic("90th percentile", 3950.milliseconds),
            Statistic("95th percentile", 4475.milliseconds),
            Statistic("99th percentile", 4895.milliseconds),
            Statistic("100th percentile (maximum)", 5000.milliseconds),
            Statistic("Average", 2000.milliseconds),
        )
    }

    @Test
    fun can_return_null_for_empty_array() = runTest {
        val result = calculateDurationStats(flowOf())
        result shouldBe null
    }

    private fun create(
        createdAt: Long,
        closedAt: Long? = null,
    ) = IssueLike(
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        closedAt = closedAt?.let { Instant.fromEpochMilliseconds(it) },
        url = "don't care",
    )
}
