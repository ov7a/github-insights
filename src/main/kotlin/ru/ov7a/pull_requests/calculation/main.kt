import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import ru.ov7a.pull_requests.calculation.ProgressReporter
import ru.ov7a.pull_requests.calculation.calculateDurationStats
import ru.ov7a.pull_requests.domain.PullRequestsStats
import ru.ov7a.pull_requests.domain.RepositoryId
import ru.ov7a.pull_requests.domain.Statistic
import ru.ov7a.pull_requests.fetcher.PullRequestsClient
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, FlowPreview::class)
suspend fun getAndCalculateStats(
    client: PullRequestsClient,
    repositoryId: RepositoryId,
    authorizationHeader: String?,
    progressReporter: ProgressReporter
): Result<PullRequestsStats?> {
    val pullRequests = client.fetchAll(repositoryId, authorizationHeader).onEach {
        progressReporter.consume(it)
    }.flatMapConcat { it.pullRequests.asFlow() }

    return runCatching {
        calculateDurationStats(pullRequests)
            ?.plus(Statistic("Total count", progressReporter.count))
    }
}