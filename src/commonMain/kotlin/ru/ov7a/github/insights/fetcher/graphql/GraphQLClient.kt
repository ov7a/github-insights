package ru.ov7a.github.insights.fetcher.graphql

import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlin.math.min
import kotlin.reflect.KProperty1
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.Filters
import ru.ov7a.github.insights.domain.input.DataBatch
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.Label
import ru.ov7a.github.insights.domain.input.RepositoryId
import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.JsonClient

abstract class AbstractGraphQLClient<Response>(
    private val json: JsonClient
) : Client {

    protected abstract val name: String
    protected abstract val customFields: String
    protected abstract val responseField: KProperty1<RepositoryResponse, DataPage<Response>?>
    protected abstract fun Response.convert(): IssueLike

    private fun createQuery(
        repositoryId: RepositoryId,
        filters: Filters,
        limit: Int,
        cursor: String?,
    ): GraphQLRequest {
        val filter = listOfNotNull(
            "last: $limit",
            filters.states?.joinToString(prefix = "states:[", postfix = "]", separator = ","),
            filters.includeLabels?.joinToString(prefix = "labels:[", postfix = "]", separator = ",") { "\"$it\"" },
            cursor?.let { "before: \"$it\"" },
        ).joinToString(", ")

        return GraphQLRequest(
            """
        {
          repository(name: "${repositoryId.name}", owner: "${repositoryId.owner}") {
            $name($filter) {
              totalCount
              nodes {
                $customFields
                url, createdAt, closedAt
                comments { totalCount }
                reactions { totalCount }
                labels(first: 100) { nodes { name, color } }
              }
              pageInfo { startCursor, hasPreviousPage }
            }
          }
        }""".trimIndent()
        )
    }

    private suspend fun fetch(
        fetchParameters: FetchParameters,
        limit: Int,
        cursor: String?,
    ): GraphQLResponse {
        return json.client.post(GRAPHQL_URL) {
            contentType(ContentType.Application.Json)
            setBody(createQuery(fetchParameters.repositoryId, fetchParameters.filters, limit, cursor))
            headers {
                append(HttpHeaders.Authorization, fetchParameters.authorizationHeader)
            }
        }.body()
    }

    private fun DataPage<Response>.toBatch(totalCount: Int): DataBatch = DataBatch(
        totalCount = totalCount,
        data = this.nodes.map { it.convert() }
    )

    protected fun LabelsResponse.convert() = nodes.map { Label(it.name, "#" + it.color) }

    override suspend fun fetchAll(fetchParameters: FetchParameters): Flow<DataBatch> =
        flow {
            val limit = fetchParameters.filters.limit
            var data = fetch(fetchParameters, min(MAX_ITEMS_PER_QUERY, limit ?: MAX_ITEMS_PER_QUERY), null)
                .data(responseField)
            val totalItemsToFetch = limit?.let { min(data.totalCount, it) } ?: data.totalCount
            val firstBatch = data.toBatch(totalItemsToFetch)
            emit(firstBatch)

            var itemsLeft: Int = totalItemsToFetch - firstBatch.data.size
            while (data.pageInfo.hasPreviousPage && itemsLeft > 0) {
                data = fetch(fetchParameters, min(MAX_ITEMS_PER_QUERY, itemsLeft), data.pageInfo.startCursor)
                    .data(responseField)
                val batch = data.toBatch(totalItemsToFetch)
                itemsLeft -= batch.data.size
                emit(batch)
            }
        }

    private companion object {
        const val GRAPHQL_URL = "https://api.github.com/graphql"
        const val MAX_ITEMS_PER_QUERY = 100
    }
}
