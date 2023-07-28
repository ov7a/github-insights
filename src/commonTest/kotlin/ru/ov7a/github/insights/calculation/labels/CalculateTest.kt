package ru.ov7a.github.insights.calculation.labels

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.Label
import ru.ov7a.github.insights.runTest

class CalculateTest {
    @Test
    fun should_calculate_result() = runTest {
        val area1 = Label("in:domain", "#0000AA")
        val area2 = Label("in:parsing", "#0000BB")
        val area3 = Label("in:presenting", "#0000CC")
        val area4 = Label("pewpew", "#DD00AA")

        val issues = flowOf(
            create(
                labels = emptyList(),
                comments = 10,
                reactions = 22,
            ),
            create(
                labels = listOf(area2),
                comments = 10,
                reactions = 22,
            ),
            create(
                labels = listOf(area1, area2),
                comments = 1,
                reactions = 3,
            ),
            create(
                labels = listOf(area1, area2, area3),
                comments = 100,
                reactions = 13,
            ),
            create(
                labels = listOf(area1, area3),
                comments = 4,
                reactions = 15,
            ),
            create(
                labels = listOf(area4),
                comments = 200,
                reactions = 300,
            ),
        )
        val result = calculateLabelsGraph(issues)
        result shouldNotBe null

        result!!.total() shouldBe 5
        result.nodes() shouldContainExactlyInAnyOrder listOf(
            WeightedLabel(area4.name, area4.color, 1, 501),
            WeightedLabel(area2.name, area2.color, 3, 152),
            WeightedLabel(area1.name, area1.color, 3, 139),
            WeightedLabel(area3.name, area3.color, 2, 134),
        )
        result.edges() shouldContainExactlyInAnyOrder listOf(
            Edge(area1.name, area2.name, 2),
            Edge(area1.name, area3.name, 2),
            Edge(area2.name, area3.name, 1),
        )
    }

    @Test
    fun can_return_null_for_empty_array() = runTest {
        val result = calculateLabelsGraph(flowOf())
        result shouldBe null
    }

    private fun create(
        labels: List<Label>,
        reactions: Int,
        comments: Int,
    ) = IssueLike(
        createdAt = Clock.System.now(),
        closedAt = Clock.System.now(),
        url = "don't care",
        labels = labels,
        reactions = reactions,
        comments = comments,
    )
}