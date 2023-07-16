package ru.ov7a.github.insights.ui.presentation

import org.w3c.dom.HTMLElement

interface Presenter<Data : Any> {
    fun render(data: Data): HTMLElement
}