package ru.ov7a.github.insights.ui.elements

import org.w3c.dom.HTMLProgressElement
import ru.ov7a.github.insights.domain.ProgressReporter

class ProgressBarReporter : ProgressReporter() {
    private val progressBar = getHtmlElement(PROGRESS_BAR_ID) as HTMLProgressElement

    init {
        progressBar.value = 0.0
    }

    override suspend fun report(value: Double) {
        progressBar.value = value * 100
    }

    private companion object {
        const val PROGRESS_BAR_ID = "fetch_progress"
    }
}