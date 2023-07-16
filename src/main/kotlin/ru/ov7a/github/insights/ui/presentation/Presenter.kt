package ru.ov7a.github.insights.ui.presentation

import org.w3c.dom.HTMLElement
import ru.ov7a.github.insights.domain.FetchParameters

interface Presenter<Data : Any> {
    fun render(fetchParameters: FetchParameters, data: Data): HTMLElement
}