package ru.ov7a.github.insights.fetcher.graphql.pulls

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.ov7a.github.insights.fetcher.graphql.CountResponse
import ru.ov7a.github.insights.fetcher.graphql.DataPage
import ru.ov7a.github.insights.fetcher.graphql.LabelsResponse
import ru.ov7a.github.insights.fetcher.graphql.RepositoryResponse

@Serializable
data class PullsRepositoryResponse(
    val pullRequests: DataPage<PullRequestResponse>
) : RepositoryResponse<PullRequestResponse> {
    override fun page(): DataPage<PullRequestResponse> = pullRequests
}

@Serializable
data class PullRequestResponse(
    val url: String,
    val createdAt: Instant,
    val closedAt: Instant? = null,
    val mergedAt: Instant? = null,
    val labels: LabelsResponse,
    val comments: CountResponse,
    val reactions: CountResponse,
)
