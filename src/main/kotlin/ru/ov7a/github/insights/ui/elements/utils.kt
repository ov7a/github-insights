package ru.ov7a.github.insights.ui.elements

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement

fun getInput(inputId: String): HTMLInputElement =
    document.getElementById(inputId) as HTMLInputElement?
        ?: throw RuntimeException("Oopsie, can't find input $inputId by id")

fun getSelector(inputId: String): HTMLSelectElement =
    document.getElementById(inputId) as HTMLSelectElement?
        ?: throw RuntimeException("Oopsie, can't find select $inputId by id")

fun getHtmlElement(id: String): HTMLElement =
    document.getElementById(id) as HTMLElement? ?: throw RuntimeException("Element not found: $id")

fun HTMLElement.hide() {
    hidden = true
}

fun HTMLElement.show() {
    hidden = false
}

fun HTMLElement.setContent(vararg content: HTMLElement) {
    innerHTML = ""
    append(*content)
}
