package ru.ov7a.github.insights.fetcher.graphql

import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.ov7a.github.insights.domain.DataBatch
import ru.ov7a.github.insights.domain.IssueLike
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.fetcher.Client
import ru.ov7a.github.insights.fetcher.JsonClient

abstract class AbstractGraphQLClient<Response>(
    private val json: JsonClient
) : Client {

    protected abstract val name: String
    protected abstract fun Response.convert(): IssueLike
    protected abstract fun getRequestFields(): String

    private fun createQuery(
        repositoryId: RepositoryId,
        cursor: String?,
    ): GraphQLRequest {
        val filter = listOfNotNull(
            "last: 100",
            cursor?.let { "before: \"$it\"" },
        ).joinToString(", ")

        return GraphQLRequest(
            """
        {
          repository(name: "${repositoryId.name}", owner: "${repositoryId.owner}") {
            $name($filter) {
              totalCount
              nodes {
                ${getRequestFields()}
              }
              pageInfo { startCursor, hasPreviousPage }
            }
          }
        }""".trimIndent()
        )
    }

    private suspend fun fetch(
        repositoryId: RepositoryId,
        cursor: String?,
        authorizationHeader: String
    ): GraphQLResponse<Response> {
        return json.client.post(GRAPHQL_URL) {
            contentType(ContentType.Application.Json)
            setBody(createQuery(repositoryId, cursor))
            headers {
                append(HttpHeaders.Authorization, authorizationHeader)
            }
        }.body()
    }

    private fun DataPage<Response>.toBatch(): DataBatch = DataBatch(
        totalCount = this.totalCount,
        data = this.nodes.map { it.convert() }
    )

    override suspend fun fetchAll(repositoryId: RepositoryId, authorizationHeader: String): Flow<DataBatch> =
        flow {
            var cursor: String? = null
            do {
                val response = fetch(repositoryId, cursor, authorizationHeader)
                val data = response.data()
                emit(data.toBatch())
                cursor = data.pageInfo.startCursor
            } while (data.pageInfo.hasPreviousPage)
        }

    private companion object {
        const val GRAPHQL_URL = "https://api.github.com/graphql"
    }
}
