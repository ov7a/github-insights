package ru.ov7a.github.insights.ui.presentation

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.HTMLElement
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.output.Stats
import ru.ov7a.github.insights.ui.humanReadableDuration

@OptIn(ExperimentalTime::class)
object StatsPresenter : Presenter<Stats> {
    override fun render(fetchParameters: FetchParameters, data: Stats): HTMLElement =
        document.create.table {
            tbody {
                data.map { stat ->
                    tr {
                        td { +stat.displayName }
                        td {
                            +when (stat.value) {
                                is Duration -> humanReadableDuration(stat.value)
                                else -> stat.value.toString()
                            }
                        }
                    }
                }
            }
        }
}