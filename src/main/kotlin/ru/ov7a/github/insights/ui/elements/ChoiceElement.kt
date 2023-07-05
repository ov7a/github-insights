package ru.ov7a.github.insights.ui.elements

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList

class ChoiceElement(
    private val elementName: String,
    private val defaultValue: String
) {
    private val parts = document
        .getElementsByName(elementName).asList()
        .map { it as HTMLInputElement }
        .associateBy { it.value }
        .also {
            if (it.isEmpty()) {
                throw RuntimeException("Can't initialize ChoiceElement for $elementName: no options found")
            } else if (defaultValue !in it) {
                throw RuntimeException("Can't initialize ChoiceElement for $elementName: default value $defaultValue not in list of values: ${it.keys}")
            }
        }

    var value: String
        get() = parts.values.single { it.checked }.value
        set(newValue) {
            val validValue = newValue.takeIf { it in parts } ?: defaultValue
            parts.forEach {
                it.value.checked = (it.key == validValue)
            }
        }
}