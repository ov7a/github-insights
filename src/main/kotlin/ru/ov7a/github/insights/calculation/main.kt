import kotlin.time.ExperimentalTime
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import ru.ov7a.github.insights.calculation.calculateDurationStats
import ru.ov7a.github.insights.domain.PullRequestsStats
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.domain.Statistic
import ru.ov7a.github.insights.fetcher.PullRequestsClient

@OptIn(ExperimentalTime::class, FlowPreview::class)
suspend fun getAndCalculateStats(
    client: PullRequestsClient,
    repositoryId: RepositoryId,
    authorizationHeader: String,
    progressReporter: ru.ov7a.github.insights.calculation.ProgressReporter
): Result<PullRequestsStats?> {
    val pullRequests = client.fetchAll(repositoryId, authorizationHeader).onEach {
        progressReporter.consume(it)
    }.flatMapConcat { it.pullRequests.asFlow() }

    return runCatching {
        calculateDurationStats(pullRequests)
            ?.plus(Statistic("Total count", progressReporter.count))
    }
}
