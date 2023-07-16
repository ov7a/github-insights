package ru.ov7a.github.insights.domain.input

import kotlinx.datetime.Instant

data class IssueLike(
    val url: String,
    val createdAt: Instant,
    val closedAt: Instant? = null,
    val labels: List<Label>,
    val comments: Int,
    val reactions: Int,
)