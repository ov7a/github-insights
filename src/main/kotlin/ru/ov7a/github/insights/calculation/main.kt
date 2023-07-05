import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import ru.ov7a.github.insights.calculation.stats.calculateDurationStats
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.ItemType
import ru.ov7a.github.insights.domain.ProgressReporter
import ru.ov7a.github.insights.domain.Statistic
import ru.ov7a.github.insights.domain.Stats
import ru.ov7a.github.insights.fetcher.Clients
import ru.ov7a.github.insights.fetcher.ClientsProvider

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
suspend fun getAndCalculateStats(
    fetchParameters: FetchParameters,
    progressReporter: ProgressReporter,
    clientsProvider: ClientsProvider = Clients
): Result<Stats?> {
    val client = when (fetchParameters.itemType) {
        ItemType.PULL -> clientsProvider.pullRequests
        ItemType.ISSUE -> clientsProvider.issues
    }
    val data = client.fetchAll(fetchParameters).onEach {
        progressReporter.consume(it)
    }.flatMapConcat { it.data.asFlow() }

    return runCatching {
        calculateDurationStats(data)
            ?.plus(Statistic("Total count", progressReporter.count))
    }
}