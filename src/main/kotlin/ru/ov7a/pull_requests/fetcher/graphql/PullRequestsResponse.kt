package ru.ov7a.pull_requests.fetcher.graphql

import kotlinx.serialization.Serializable
import ru.ov7a.pull_requests.domain.PullRequest

@Serializable
data class GraphQLPullRequestsResponse(
    val data: RepositoryDataResponse
)

@Serializable
data class RepositoryDataResponse(
    val repository: RepositoryResponse
)

@Serializable
data class RepositoryResponse(
    val pullRequests: PullRequestsResponse
)

@Serializable
data class PullRequestsResponse(
    val totalCount: Int,
    val nodes: List<PullRequest>,
    val pageInfo: PageInfoResponse
)

@Serializable
data class PageInfoResponse(
    val startCursor: String?,
    val hasPreviousPage: Boolean
)