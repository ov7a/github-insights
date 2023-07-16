package ru.ov7a.github.insights.calculation

import getAndCalculate
import io.kotest.assertions.withClue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import ru.ov7a.github.insights.calculation.stats.calculateResolveTime
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.Filters
import ru.ov7a.github.insights.domain.ProgressReporter
import ru.ov7a.github.insights.domain.input.DataBatch
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.domain.input.RepositoryId
import ru.ov7a.github.insights.domain.output.Statistic
import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.ClientsProvider
import ru.ov7a.github.insights.runTest

@OptIn(ExperimentalTime::class)
class MainTests {

    @Test
    fun should_collect_stats() = runTest {
        val result = executeOn(
            listOf(
                DataBatch(totalCount = 3, listOf(itemStub(), itemStub())),
                DataBatch(totalCount = 3, listOf(itemStub())),
            ),
            calculator = ::calculateResolveTime
        )

        withClue(result) {
            result.isSuccess shouldBe true
        }
        val stats = result.getOrNull()
        stats?.size shouldBe 14 + 1
        stats?.last() shouldBe Statistic("Total count", 3)
    }

    @Test
    fun should_catch_error_from_client() = runTest {
        val result = executeOn(
            listOf(DataBatch(10, emptyList())),
            throwError = true,
            calculator = ::calculateResolveTime
        )

        withClue(result) {
            result.isFailure shouldBe true
        }
        result.exceptionOrNull() should beInstanceOf<Exception>()
    }

    @Test
    fun should_catch_error_from_calculator() = runTest {
        val result = executeOn(
            listOf(
                DataBatch(totalCount = 3, listOf(itemStub(), itemStub())),
                DataBatch(totalCount = 3, listOf(itemStub())),
            )
        ) { data ->
            calculateResolveTime(data)
            throw RuntimeException("oops")
        }

        withClue(result) {
            result.isFailure shouldBe true
        }
        result.exceptionOrNull() should beInstanceOf<Exception>()
    }

    @Test
    fun should_process_empty_result() = runTest {
        val result = executeOn(
            emptyList(),
            calculator = ::calculateResolveTime
        )

        withClue(result) {
            result.isSuccess shouldBe true
        }
        result.getOrNull() shouldBe null
    }

    @Test
    fun should_process_empty_batch() = runTest {
        val result = executeOn(
            listOf(DataBatch(0, emptyList())),
            calculator = ::calculateResolveTime
        )

        withClue(result) {
            result.isSuccess shouldBe true
        }
        result.getOrNull() shouldBe null
    }

    private suspend fun <Data : Any> executeOn(
        data: List<DataBatch>,
        throwError: Boolean = false,
        calculator: suspend (Flow<IssueLike>) -> Data?
    ): Result<Data?> {
        val client = clientsProvider(data, throwError)

        val reporter = ProgressReporter()

        return getAndCalculate(
            mockFetchParameters,
            reporter,
            client,
            calculator
        )
    }

    private val mockFetchParameters = FetchParameters(
        ItemType.PULL,
        RepositoryId("octocat", "Hello-World"),
        Filters(),
        "don't care",
    )

    private fun itemStub() = IssueLike(
        url = "don't care",
        createdAt = Clock.System.now(),
        labels = emptyList(),
        reactions = 1,
        comments = 2,
    )

    private fun clientsProvider(
        data: List<DataBatch>,
        throwError: Boolean = false,
    ): ClientsProvider = object : ClientsProvider {
        override val pullRequests: Client = MockClient()
        override val issues: Client = MockClient()

        inner class MockClient : Client {
            override suspend fun fetchAll(fetchParameters: FetchParameters): Flow<DataBatch> {
                return if (throwError) {
                    flow {
                        emit(data.first())
                        throw RuntimeException("error happened")
                    }
                } else {
                    data.asFlow()
                }
            }
        }
    }
}
