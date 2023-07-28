package ru.ov7a.github.insights.calculation.stats

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class PercentileTests {
    @Test
    fun should_provide_proper_values_for_small_arrays() {
        val testValues = listOf(15.0, 20.0, 35.0, 40.0, 50.0)

        percentile(testValues, 9) shouldBe 16.8
        percentile(testValues, 10) shouldBe 17
        percentile(testValues, 15) shouldBe 18
        percentile(testValues, 20) shouldBe 19
        percentile(testValues, 30) shouldBe 23
        percentile(testValues, 40) shouldBe 29
        percentile(testValues, 50) shouldBe 35
        percentile(testValues, 60) shouldBe 37
        percentile(testValues, 70) shouldBe 39
        percentile(testValues, 80) shouldBe 42
        percentile(testValues, 90) shouldBe 46
    }

    @Test
    fun should_provide_proper_value_for_even_mean() {
        val testValues = listOf(15.0, 20.0, 35.0, 40.0)

        percentile(testValues, 50) shouldBe (20.0 + 35.0) / 2.0
    }

    @Test
    fun should_provide_proper_value_for_0() {
        val testValues = listOf(15.0, 20.0, 35.0, 40.0)

        percentile(testValues, 0) shouldBe testValues.first()
    }

    @Test
    fun should_provide_proper_value_for_100() {
        val testValues = listOf(15.0, 20.0, 35.0, 40.0)

        percentile(testValues, 100) shouldBe testValues.last()
    }

    @Test
    fun should_provide_proper_values_for_big_arrays() {
        val testValues = List(1000) { 1.0 } + listOf(1_000_000.0)

        percentile(testValues, 9) shouldBe 1
        percentile(testValues, 10) shouldBe 1
        percentile(testValues, 20) shouldBe 1
        percentile(testValues, 50) shouldBe 1
        percentile(testValues, 90) shouldBe 1
        percentile(testValues, 99) shouldBe 1
        percentile(testValues, 100) shouldBe testValues.last()
    }

    @Test
    fun should_fail_on_values_below_zero() {
        val testValues = listOf(15.0, 20.0)
        val exception = shouldThrow<IllegalArgumentException> {
            percentile(testValues, -1)
        }
        exception.message shouldContain "non-negative"
    }

    @Test
    fun should_fail_on_values_above_100() {
        val testValues = listOf(15.0, 20.0)
        val exception = shouldThrow<IllegalArgumentException> {
            percentile(testValues, 101)
        }
        exception.message shouldContain "100"
    }

    @Test
    fun should_fail_on_empty_arrays() {
        val exception = shouldThrow<IllegalArgumentException> {
            percentile(emptyList<Double>(), 50)
        }
        exception.message shouldContain "empty"
    }

    @Test
    fun should_work_properly_on_tiny_arrays() {
        val oneValue = listOf(10.0)
        percentile(oneValue, 0) shouldBe oneValue.first()
        percentile(oneValue, 50) shouldBe oneValue.first()
        percentile(oneValue, 100) shouldBe oneValue.first()

        val twoValues = listOf(10.0, 20.0)
        percentile(twoValues, 0) shouldBe twoValues.first()
        percentile(twoValues, 50) shouldBe 15.0
        percentile(twoValues, 100) shouldBe twoValues.last()
    }
}