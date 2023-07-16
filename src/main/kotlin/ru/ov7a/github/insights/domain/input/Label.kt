package ru.ov7a.github.insights.domain.input

data class Label(
    val name: LabelId,
    val color: Color
)

typealias LabelId = String
typealias Color = String
