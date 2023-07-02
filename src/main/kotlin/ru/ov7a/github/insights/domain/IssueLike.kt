package ru.ov7a.github.insights.domain

import kotlinx.datetime.Instant

data class IssueLike(
    val url: String,
    val createdAt: Instant,
    val closedAt: Instant? = null,
)
