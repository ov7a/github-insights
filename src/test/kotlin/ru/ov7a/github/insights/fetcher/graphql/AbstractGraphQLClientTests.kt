package ru.ov7a.github.insights.fetcher.graphql

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.name
import kotlin.js.json
import kotlin.test.Test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import ru.ov7a.github.insights.Endpoint
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.Filters
import ru.ov7a.github.insights.domain.State
import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.domain.input.RepositoryId
import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.JsonClient
import ru.ov7a.github.insights.fetcher.withMockEngine
import ru.ov7a.github.insights.loadResource
import ru.ov7a.github.insights.mockResponse
import ru.ov7a.github.insights.response
import ru.ov7a.github.insights.runTest

abstract class AbstractGraphQLClientTests {

    protected companion object {
        const val url = "https://api.github.com/graphql"
        const val authHeader = "Basic someBase64String"

        val defaultRequestHeaders = mapOf(
            HttpHeaders.Authorization to listOf(authHeader),
            HttpHeaders.Accept to listOf(ContentType.Application.Json.toString()),
            HttpHeaders.AcceptCharset to listOf(Charsets.UTF_8.name),
        )

        val defaultResponseHeaders = mapOf(
            HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
        )

        val endpoint = Endpoint(
            url = url,
            method = HttpMethod.Post,
            headers = defaultRequestHeaders
        )

        fun graphQlQuery(resource: String) = JSON.stringify(
            json("query" to loadResource(resource))
        )

        fun validResponse(resource: String) = response(
            content = loadResource(resource),
            headers = defaultResponseHeaders
        )
    }

    protected abstract val dataDir: String
    protected abstract val itemType: ItemType

    abstract fun createClient(jsonClient: JsonClient): Client

    protected suspend fun <T> makeQuery(
        expectedPayloadResource: String,
        respondWith: MockRequestHandler,
        action: suspend Client.() -> T,
    ): T = withMockEngine(
        createClient = ::createClient,
        handlers = arrayOf(
            mockResponse(
                endpoint = endpoint,
                expectedBody = graphQlQuery(expectedPayloadResource),
                response = respondWith
            )
        ),
        action = action,
    )

    protected val defaultFetchParameters: FetchParameters by lazy {
        FetchParameters(
            itemType,
            RepositoryId("octocat", "Hello-World"),
            Filters(),
            authHeader
        )
    }

    @Test
    fun should_properly_fetch_several_pages() = runTest {
        fun responseMock(dataFile: String) = mockResponse(
            endpoint,
            graphQlQuery("requests/graphql/$dataDir/$dataFile.graphql"),
            validResponse("responses/graphql/$dataDir/$dataFile.json"),
        )

        val result = withMockEngine(
            ::createClient,
            responseMock(dataFile = "page1"),
            responseMock(dataFile = "page2"),
            responseMock(dataFile = "page3")
        ) {
            fetchAll(defaultFetchParameters).toList()
        }

        result shouldHaveSize 3
        result.flatMap { it.data } shouldHaveSize 7
    }

    @Test
    fun fetch_all_should_rethrow_exception() = runTest {
        val exception = shouldThrow<ClientRequestException> {
            val resultFlow = makeQuery(
                "requests/graphql/$dataDir/non-existing.graphql",
                response(
                    statusCode = HttpStatusCode.NotFound,
                    content = "",
                    headers = defaultResponseHeaders
                )
            ) {
                fetchAll(
                    defaultFetchParameters.copy(repositoryId = RepositoryId("octocat", "non-existing"))
                )
            }
            resultFlow.collect()
        }
        exception.message shouldContain "404 Not Found"
    }

    @Test
    fun fetch_all_should_rethrow_data_exception() = runTest {
        val exception = shouldThrow<GraphQLError> {
            val resultFlow = makeQuery(
                "requests/graphql/$dataDir/example.graphql",
                validResponse("responses/graphql/page_data_error.json"),
            ) {
                fetchAll(defaultFetchParameters)
            }
            resultFlow.collect()
        }
        exception.message shouldContain "Could not resolve to a Repository with the name 'some/typo'"
    }

    @Test
    fun fetch_all_should_rethrow_query_exception() = runTest {
        val exception = shouldThrow<GraphQLError> {
            val resultFlow = makeQuery(
                "requests/graphql/$dataDir/example.graphql",
                validResponse("responses/graphql/page_query_error.json"),
            ) {
                fetchAll(defaultFetchParameters)
            }
            resultFlow.collect()
        }
        exception.message shouldContain "Could not parse the query"
    }

    @Test
    fun should_filter_and_limit() = runTest {
        val limit = 2
        val filters = Filters(
            limit = limit,
            includeLabels = setOf("a:bug", "a:feature"),
            states = setOf(State.OPEN),
        )
        val result = makeQuery(
            "requests/graphql/$dataDir/filtered.graphql",
            validResponse("responses/graphql/$dataDir/example_page.json"),
        ) {
            fetchAll(defaultFetchParameters.copy(filters = filters)).toList()
        }
        val batch = result.single()
        batch.totalCount shouldBe limit
    }
}
