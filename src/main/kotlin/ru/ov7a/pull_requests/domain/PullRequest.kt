package ru.ov7a.pull_requests.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class PullRequestState {
    OPEN,
    CLOSED,
    MERGED
}

@Serializable
data class PullRequest(
    val url: String,
    val state: PullRequestState,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val closedAt: Instant? = null,
    val mergedAt: Instant? = null
)