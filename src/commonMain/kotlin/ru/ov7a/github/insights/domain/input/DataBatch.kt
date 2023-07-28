package ru.ov7a.github.insights.domain.input

data class DataBatch(
    val totalCount: Int,
    val data: List<IssueLike>,
)