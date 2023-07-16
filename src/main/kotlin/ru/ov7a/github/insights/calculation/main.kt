import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.ProgressReporter
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.fetcher.Clients
import ru.ov7a.github.insights.fetcher.ClientsProvider

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> getAndCalculate(
    fetchParameters: FetchParameters,
    progressReporter: ProgressReporter,
    clientsProvider: ClientsProvider = Clients,
    calculator: suspend (Flow<IssueLike>) -> T?,
): Result<T?> {
    val client = when (fetchParameters.itemType) {
        ItemType.PULL -> clientsProvider.pullRequests
        ItemType.ISSUE -> clientsProvider.issues
    }
    val data: Flow<IssueLike> = client.fetchAll(fetchParameters).onEach {
        progressReporter.consume(it)
    }.flatMapConcat { it.data.asFlow() }

    return runCatching {
        calculator(data)
    }
}