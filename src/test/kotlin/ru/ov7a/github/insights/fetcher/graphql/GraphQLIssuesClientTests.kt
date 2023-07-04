package ru.ov7a.github.insights.fetcher.graphql

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import ru.ov7a.github.insights.domain.DataBatch
import ru.ov7a.github.insights.domain.IssueLike
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.JsonClient
import ru.ov7a.github.insights.fetcher.graphql.issues.IssuesClient
import ru.ov7a.github.insights.runTest

class GraphQLIssuesClientTests : AbstractGraphQLClientTests() {

    override fun createClient(jsonClient: JsonClient): Client = IssuesClient(jsonClient)
    override val dataDir = "issues"

    @Test
    fun should_fetch_single_page_properly() = runTest {
        val result = makeQuery(
            "requests/graphql/$dataDir/example.graphql",
            validResponse("responses/graphql/$dataDir/example_page.json"),
        ) {
            fetchAll(RepositoryId("octocat", "Hello-World"), authHeader).toList()
        }

        result shouldBe listOf(
            DataBatch(
                totalCount = 370,
                data = listOf(
                    IssueLike(
                        url = "https://github.com/octocat/Hello-World/issue/1046",
                        createdAt = Instant.fromEpochMilliseconds(1296068472_000),
                        closedAt = Instant.fromEpochMilliseconds(1296068592_000),
                    ),
                    IssueLike(
                        url = "fake_url",
                        createdAt = Instant.fromEpochMilliseconds(1332878745_000)
                    ),
                    IssueLike(
                        url = "fake_url2",
                        createdAt = Instant.fromEpochMilliseconds(1296068472_000),
                        closedAt = Instant.fromEpochMilliseconds(1296068592_000),
                    ),
                ),
            )
        )
    }
}
