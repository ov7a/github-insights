package ru.ov7a.github.insights.domain

data class DataBatch(
    val totalCount: Int,
    val data: List<IssueLike>,
)
