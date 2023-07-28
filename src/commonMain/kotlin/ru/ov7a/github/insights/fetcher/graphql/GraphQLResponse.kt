package ru.ov7a.github.insights.fetcher.graphql

import kotlin.reflect.KProperty1
import kotlinx.serialization.Serializable
import ru.ov7a.github.insights.fetcher.graphql.issues.IssueResponse
import ru.ov7a.github.insights.fetcher.graphql.pulls.PullRequestResponse

@Serializable
data class GraphQLResponse(
    private val data: DataResponse? = null,
    val errors: List<ErrorResponse> = emptyList(),
) {
    fun <T> data(field: KProperty1<RepositoryResponse, DataPage<T>?>): DataPage<T> = data?.repository?.let(field)
        ?: throw GraphQLError(errors.firstOrNull()?.message ?: "Unknown error during fetching data")
}

@Serializable
data class DataResponse(
    val repository: RepositoryResponse? = null,
)

@Serializable
data class RepositoryResponse(
    // This solution is not the best, however previous one was also a crutch. At least this works in native.
    val issues: DataPage<IssueResponse>? = null,
    val pullRequests: DataPage<PullRequestResponse>? = null,
)

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

@Serializable
data class CountResponse(
    val totalCount: Int,
)

@Serializable
data class LabelsResponse(
    val nodes: List<LabelResponse>,
)

@Serializable
data class LabelResponse(
    val name: String,
    val color: String,
)
