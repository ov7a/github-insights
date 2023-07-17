package ru.ov7a.github.insights.ui.elements

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement

fun getInput(id: String): HTMLInputElement =
    getHtmlElement(id) as? HTMLInputElement?
        ?: throw RuntimeException("Element $id is not input")

fun getSelector(id: String): HTMLSelectElement =
    getHtmlElement(id) as? HTMLSelectElement?
        ?: throw RuntimeException("Element $id is not select")

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
