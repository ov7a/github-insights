package ru.ov7a.github.insights.calculation

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.ov7a.github.insights.domain.PullRequest
import ru.ov7a.github.insights.domain.PullRequestsStats
import ru.ov7a.github.insights.domain.Statistic

@ExperimentalTime
fun getDuration(pullRequest: PullRequest, now: Instant): Duration =
    (pullRequest.closedAt ?: pullRequest.mergedAt ?: now) - pullRequest.createdAt


@OptIn(ExperimentalTime::class)
suspend fun calculateDurationStats(
    pullRequests: Flow<PullRequest>,
    now: Instant = Clock.System.now()
): PullRequestsStats? {
    val durations = pullRequests.map { pullRequest -> getDuration(pullRequest, now) }

    return getStats(durations)
}

@ExperimentalTime
private val statsGetters = listOf<Pair<String, (List<Duration>) -> Duration>>(
    "0th percentile (minimum)" to { it.first() },
    "10th percentile" to { percentile(it, 10) },
    "20th percentile" to { percentile(it, 20) },
    "30th percentile" to { percentile(it, 30) },
    "40th percentile" to { percentile(it, 40) },
    "50th percentile (mean)" to { percentile(it, 50) },
    "60th percentile" to { percentile(it, 60) },
    "70th percentile" to { percentile(it, 70) },
    "80th percentile" to { percentile(it, 80) },
    "90th percentile" to { percentile(it, 90) },
    "95th percentile" to { percentile(it, 95) },
    "99th percentile" to { percentile(it, 99) },
    "100th percentile (maximum)" to { values -> values.last() },
    "Average" to { it.reduce { a, b -> a + b }.times(1.0 / it.size) },
)

@ExperimentalTime
suspend fun getStats(durations: Flow<Duration>): PullRequestsStats? {
    val sortedDurations = durations.toList().sorted()

    if (sortedDurations.isEmpty()) {
        return null
    }

    return statsGetters.map { (name, getter) ->
        Statistic(name, getter(sortedDurations))
    }
}
