package ru.ov7a.github.insights.ui

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.browser.window

fun catchValidationError(block: () -> Unit) {
    try {
        block()
    } catch (ex: ValidationException) {
        window.alert(ex.message)
    }
}

@ExperimentalTime
fun humanReadableDuration(duration: Duration): String {
    val truncated = duration.toComponents { days, hours, minutes, seconds, _ ->
        when {
            days > 7 -> days.days
            days > 0 -> days.days + hours.hours
            hours > 0 -> hours.hours + minutes.minutes
            else -> minutes.minutes + seconds.seconds
        }
    }
    return truncated.toString()
}

class ValidationException(override val message: String) : IllegalArgumentException(message)

external fun encodeURIComponent(encodedURI: String): String