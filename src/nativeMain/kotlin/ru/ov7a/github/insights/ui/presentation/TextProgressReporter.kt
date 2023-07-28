package ru.ov7a.github.insights.ui.presentation

import kotlin.math.floor
import ru.ov7a.github.insights.domain.ProgressReporter

class TextProgressReporter : ProgressReporter() {

    private val width = 50
    override suspend fun report(value: Double) {
        val filled = floor(width * value).toInt()
        val percent = floor(100 * value).toInt().toString().padStart(3)
        printErr("\rFetching: [${"#".repeat(filled)}${".".repeat(width - filled)}] $percent%")
        if (value == 1.0) {
            printlnErr("")
        }
    }
}
