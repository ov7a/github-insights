package ru.ov7a.github.insights.fetcher.graphql.issues

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.ov7a.github.insights.fetcher.graphql.CountResponse
import ru.ov7a.github.insights.fetcher.graphql.LabelsResponse

@Serializable
data class IssueResponse(
    val url: String,
    val createdAt: Instant,
    val closedAt: Instant? = null,
    val labels: LabelsResponse,
    val comments: CountResponse,
    val reactions: CountResponse,
)
