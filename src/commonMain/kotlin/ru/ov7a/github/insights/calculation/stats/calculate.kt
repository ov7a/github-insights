package ru.ov7a.github.insights.calculation.stats

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.output.Statistic
import ru.ov7a.github.insights.domain.output.Stats

@ExperimentalTime
fun getDuration(issueLike: IssueLike, now: Instant): Duration =
    (issueLike.closedAt ?: now) - issueLike.createdAt

@OptIn(ExperimentalTime::class)
suspend fun calculateResolveTime(data: Flow<IssueLike>) = calculateDurationStats(data)

@OptIn(ExperimentalTime::class)
suspend fun calculateDurationStats(
    data: Flow<IssueLike>,
    now: Instant = Clock.System.now()
): Stats? {
    val durations = data.map { issueLike -> getDuration(issueLike, now) }

    return getStats(durations)
}

@ExperimentalTime
private val statsGetters = listOf<Pair<String, (List<Duration>) -> Any>>(
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
    "Total count" to { it.size },
)

@ExperimentalTime
private suspend fun getStats(durations: Flow<Duration>): Stats? {
    val sortedDurations = durations.toList().sorted()

    if (sortedDurations.isEmpty()) {
        return null
    }

    return statsGetters.map { (name, getter) ->
        Statistic(name, getter(sortedDurations))
    }
}
