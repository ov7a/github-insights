package ru.ov7a.github.insights.ui

import kotlinx.browser.window

fun catchValidationError(block: () -> Unit) {
    try {
        block()
    } catch (ex: ValidationException) {
        window.alert(ex.message)
    }
}

class ValidationException(override val message: String) : IllegalArgumentException(message)

external fun encodeURIComponent(encodedURI: String): String
