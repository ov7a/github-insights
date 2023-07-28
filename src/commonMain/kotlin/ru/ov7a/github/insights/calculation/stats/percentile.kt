package ru.ov7a.github.insights.calculation.stats

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.truncate
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

//see https://en.wikipedia.org/wiki/Flow_(mathematics)
private interface GroupWithFlow<T> {
    fun plus(value: T, other: T): T
    fun minus(value: T, other: T): T
    fun times(value: T, multiplier: Double): T
}

@ExperimentalTime
private object DurationGroupWithFlow : GroupWithFlow<Duration> {
    override fun plus(value: Duration, other: Duration) = value + other

    override fun minus(value: Duration, other: Duration) = value - other

    override fun times(value: Duration, multiplier: Double) = value.times(multiplier)
}

private object DoubleGroupWithFlow : GroupWithFlow<Double> {
    override fun plus(value: Double, other: Double) = value + other

    override fun minus(value: Double, other: Double) = value - other

    override fun times(value: Double, multiplier: Double) = multiplier * value
}

private fun <T> percentile(values: List<T>, percentile: Int, operations: GroupWithFlow<T>): T {
    require(percentile >= 0) { "Percentile must be non-negative" }
    require(percentile <= 100) { "Percentile must be less than or equal to 100" }
    require(values.isNotEmpty()) { "Values shouldn't be empty" }

    val index = percentile * 0.01 * (values.size - 1)
    val lowerIndex = floor(index).toInt()
    val upperIndex = ceil(index).toInt()
    val indexFraction = index - truncate(index)
    return operations.plus(
        values[lowerIndex],
        operations.times(
            operations.minus(values[upperIndex], values[lowerIndex]),
            indexFraction
        )
    )
}

fun percentile(values: List<Double>, percentile: Int) = percentile(values, percentile, DoubleGroupWithFlow)

@ExperimentalTime
fun percentile(values: List<Duration>, percentile: Int) = percentile(values, percentile, DurationGroupWithFlow)
