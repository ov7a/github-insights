package ru.ov7a.github.insights.fetcher.graphql

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLResponse<T>(
    private val data: DataResponse<T>? = null,
    val errors: List<ErrorResponse> = emptyList(),
) {
    fun data(): DataPage<T> = data?.repository?.page()
        ?: throw GraphQLError(errors.firstOrNull()?.message ?: "Unknown error during fetching data")
}

@Serializable
data class DataResponse<T>(
    val repository: RepositoryResponse<T>? = null,
)

@Polymorphic
interface RepositoryResponse<T> {
    fun page(): DataPage<T>
}

@Serializable
data class DataPage<T>(
    val totalCount: Int,
    val nodes: List<T>,
    val pageInfo: PageInfoResponse,
)

@Serializable
data class PageInfoResponse(
    val startCursor: String?,
    val hasPreviousPage: Boolean,
)

@Serializable
data class ErrorResponse(
    val message: String,
)
