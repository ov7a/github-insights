package ru.ov7a.github.insights.fetcher.graphql.issues

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.ov7a.github.insights.fetcher.graphql.DataPage
import ru.ov7a.github.insights.fetcher.graphql.RepositoryResponse

@Serializable
data class IssuesRepositoryResponse(
    val issues: DataPage<IssueResponse>,
) : RepositoryResponse<IssueResponse> {
    override fun page(): DataPage<IssueResponse> = issues
}

@Serializable
data class IssueResponse(
    val url: String,
    val createdAt: Instant,
    val closedAt: Instant? = null,
)
