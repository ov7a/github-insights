package ru.ov7a.github.insights.ui.presentation

import kotlin.time.ExperimentalTime
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.output.Stats

@OptIn(ExperimentalTime::class)
object StatsPresenter : Presenter<Stats> {
    override fun print(fetchParameters: FetchParameters, data: Stats) {
        data.forEach { stat ->
            println("${stat.displayName},${stat.value}")
        }
    }
}
