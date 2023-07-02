package ru.ov7a.github.insights.domain

data class PullRequestsBatch(
    val totalCount: Int,
    val pullRequests: List<PullRequest>
)
