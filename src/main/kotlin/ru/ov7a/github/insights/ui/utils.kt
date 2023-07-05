package ru.ov7a.github.insights.ui

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

fun getInput(inputId: String): HTMLInputElement =
    document.getElementById(inputId) as HTMLInputElement?
        ?: throw RuntimeException("Oopsie, can't find text input $inputId by id")

fun getHtmlElement(id: String): HTMLElement =
    document.getElementById(id) as HTMLElement? ?: throw RuntimeException("Element not found: $id")

fun HTMLElement.hide() {
    hidden = true
}

fun HTMLElement.show() {
    hidden = false
}

fun HTMLElement.setContent(content: HTMLElement) {
    innerHTML = ""
    append(content)
}

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
