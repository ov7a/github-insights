package ru.ov7a.github.insights.ui.presentation

import ru.ov7a.github.insights.calculation.labels.LabelsGraph
import ru.ov7a.github.insights.domain.FetchParameters

object LabelsPresenter : Presenter<LabelsGraph> {
    override fun print(fetchParameters: FetchParameters, data: LabelsGraph) {
        data.nodes().forEach { node ->
            println("${node.name},${node.items},${node.score}")
        }
    }
}
