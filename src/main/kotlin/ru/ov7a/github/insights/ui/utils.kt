package ru.ov7a.github.insights.ui

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

fun getTextInput(inputId: String): HTMLInputElement =
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
            days > 7 -> Duration.days(days)
            days > 0 -> Duration.days(days) + Duration.hours(hours)
            hours > 0 -> Duration.hours(hours) + Duration.minutes(minutes)
            else -> Duration.minutes(minutes) + Duration.seconds(seconds)
        }
    }
    return truncated.toString()
}
