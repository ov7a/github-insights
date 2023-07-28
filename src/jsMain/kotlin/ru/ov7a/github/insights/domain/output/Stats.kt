package ru.ov7a.github.insights.domain.output

import kotlin.time.ExperimentalTime

@ExperimentalTime
typealias Stats = List<Statistic<Any>>

data class Statistic<T>(val displayName: String, val value: T)

