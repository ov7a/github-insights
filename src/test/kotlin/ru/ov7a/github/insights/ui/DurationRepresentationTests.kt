package ru.ov7a.github.insights.ui

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.parse
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DurationRepresentationTests {

    @Test
    fun should_present_properly() {
        val expectedTransformation = listOf(
            parse("375d 6h 5m") to "375d",
            parse("45d 6h 5m 10.3s") to "45d",
            parse("5d 6h 5m 10.3s") to "5d 6h",
            parse("16h 5m 10.3s") to "16h 5m",
            parse("5m 10.3s") to "5m 10s",
            parse("23.45s") to "23s",
        )

        expectedTransformation.forEach { (input, expected) ->
            humanReadableDuration(input) shouldBe expected
        }
    }
}
