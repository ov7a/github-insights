import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import ru.ov7a.github.insights.calculation.ProgressReporter
import ru.ov7a.github.insights.calculation.calculateDurationStats
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.domain.Statistic
import ru.ov7a.github.insights.domain.Stats
import ru.ov7a.github.insights.fetcher.Client

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
suspend fun getAndCalculateStats(
    client: Client,
    repositoryId: RepositoryId,
    authorizationHeader: String,
    progressReporter: ProgressReporter
): Result<Stats?> {
    val data = client.fetchAll(repositoryId, authorizationHeader).onEach {
        progressReporter.consume(it)
    }.flatMapConcat { it.data.asFlow() }

    return runCatching {
        calculateDurationStats(data)
            ?.plus(Statistic("Total count", progressReporter.count))
    }
}
