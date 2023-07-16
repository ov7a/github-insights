package ru.ov7a.github.insights.ui.presentation

import kotlinx.browser.document
import kotlinx.html.A
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.w3c.dom.HTMLElement
import ru.ov7a.github.insights.calculation.labels.LabelsGraph
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.input.Color
import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.domain.input.RepositoryId
import ru.ov7a.github.insights.ui.encodeURIComponent

object LabelsPresenter : Presenter<LabelsGraph> {
    override fun render(fetchParameters: FetchParameters, data: LabelsGraph): HTMLElement {
        val itemName = when (fetchParameters.itemType) {
            ItemType.ISSUE -> "issue"
            ItemType.PULL -> "PR"
        }
        val repositoryId = fetchParameters.repositoryId
        val entries = data.nodes().sortedByDescending { it.score }
        return document.create.div {
            p {
                +"Total ${itemName}s: ${data.total()}"
            }
            a {
                val csv = entries.joinToString(prefix = "Label,Number of ${itemName}s,Score\n", separator = "\n") {
                    "${it.name.replace(",", "\\,")},${it.items},${it.score}"
                }
                createDownloadLink(
                    data = csv,
                    fileName = "${repositoryId.owner}_${repositoryId.name}_${itemName}s_label_stats.csv",
                )
                +"Download as csv"
            }
            table {
                thead {
                    tr {
                        th { +"Label" }
                        th { +"Number of ${itemName}s" }
                        th { +"Score" }
                    }
                }
                tbody {
                    entries.map { link ->
                        tr {
                            td {
                                a {
                                    href =
                                        getLabelLink(fetchParameters.repositoryId, fetchParameters.itemType, link.name)
                                    style = "background-color:${link.color}; color:${adaptiveColor(link.color)};"
                                    target = "_blank"
                                    +link.name
                                }
                            }
                            td { +link.items.toString() }
                            td { +link.score.toString() }
                        }
                    }
                }
            }
        }
    }

    private fun getLabelLink(repositoryId: RepositoryId, itemType: ItemType, label: String): String {
        val itemFilter = when (itemType) {
            ItemType.ISSUE -> "is:issue"
            ItemType.PULL -> "is:pr"
        }
        val query = encodeURIComponent("$itemFilter is:open label:$label")
        return "https://github.com/${repositoryId.owner}/${repositoryId.name}/issues?q=$query"
    }

    private val brightnessCache: MutableMap<Color, Int> = mutableMapOf()

    private fun calculatePerceivedBrightness(color: String): Int {
        val red = color.substring(1, 3).toInt(radix = 16)
        val green = color.substring(3, 5).toInt(radix = 16)
        val blue = color.substring(5, 7).toInt(radix = 16)
        return (red * 299 + green * 587 + blue * 114) / 1000
    }

    private fun adaptiveColor(color: Color): Color {
        val brightness = brightnessCache[color] ?: calculatePerceivedBrightness(color).apply {
            brightnessCache[color] = this
        }

        return if (brightness > 128) {
            "#000000"
        } else {
            "#FFFFFF"
        }
    }

    private fun A.createDownloadLink(
        data: String,
        fileName: String,
    ) {
        href = "data:text/plain;charset=utf-8," + encodeURIComponent(data)
        attributes["download"] = fileName
    }
}
