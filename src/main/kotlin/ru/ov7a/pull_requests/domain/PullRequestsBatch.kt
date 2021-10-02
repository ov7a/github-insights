package ru.ov7a.pull_requests.domain

data class PullRequestsBatch(
    val totalCount: Int,
    val pullRequests: List<PullRequest>
)
