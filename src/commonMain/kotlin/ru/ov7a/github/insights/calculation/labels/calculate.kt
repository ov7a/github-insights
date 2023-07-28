package ru.ov7a.github.insights.calculation.labels

import kotlinx.coroutines.flow.Flow
import ru.ov7a.github.insights.domain.input.Color
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.Label
import ru.ov7a.github.insights.domain.input.LabelId

suspend fun calculateLabelsGraph(data: Flow<IssueLike>): LabelsGraph? {
    val graph = LabelsGraph()
    data.collect { item ->
        val weight = 1 + item.comments + item.reactions
        var counted = false
        item.labels.forEachIndexed { index, label ->
            graph.addNode(label, weight)
            if (!counted) {
                graph.incrementItems()
                counted = true
            }
            for (j in index + 1 until item.labels.size) {
                val other = item.labels[j]
                graph.addEdge(label, other)
            }
        }
    }
    return graph.takeIf { !it.isEmpty() }
}

data class LabelsGraph(
    private var totalItems: Int = 0,
    private val labels: MutableSet<Label> = mutableSetOf(),
    private val scores: MutableMap<LabelId, Int> = mutableMapOf(),
    private val issues: MutableMap<LabelId, Int> = mutableMapOf(),
    private val edges: MutableMap<LabelId, MutableMap<LabelId, Int>> = mutableMapOf()
) {
    fun addNode(node: Label, weight: Int) {
        if (labels.add(node)) {
            scores[node.name] = weight
            issues[node.name] = 1
        } else {
            scores[node.name] = scores[node.name]!! + weight
            issues[node.name] = issues[node.name]!! + 1
        }
    }

    fun addEdge(from: Label, to: Label) {
        if (from.name <= to.name) {
            val fromMap = edges.getOrPut(from.name) { mutableMapOf() }
            fromMap[to.name] = (fromMap[to.name] ?: 0) + 1
        } else {
            addEdge(to, from)
        }
    }

    fun incrementItems() {
        totalItems += 1
    }

    fun isEmpty() = scores.isEmpty() && edges.isEmpty()

    fun nodes(): List<WeightedLabel> = labels.map {
        WeightedLabel(it.name, it.color, issues.getValue(it.name), scores.getValue(it.name))
    }

    fun edges(): List<Edge> = edges.flatMap { source ->
        source.value.map { Edge(source.key, it.key, it.value) }
    }

    fun total(): Int = totalItems
}

data class Edge(
    val source: LabelId,
    val target: LabelId,
    val weight: Int,
)

data class WeightedLabel(
    val name: LabelId,
    val color: Color,
    val items: Int,
    val score: Int,
)