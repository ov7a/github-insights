package ru.ov7a.github.insights.ui.contexts

import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.option
import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.domain.State
import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.domain.input.RepositoryId
import ru.ov7a.github.insights.domain.input.RequestType
import ru.ov7a.github.insights.ui.elements.ChoiceElement
import ru.ov7a.github.insights.ui.elements.getInput
import ru.ov7a.github.insights.ui.elements.getSelector
import ru.ov7a.github.insights.ui.elements.setContent
import ru.ov7a.github.insights.ui.encodeURIComponent

class InputContext {
    private val repoIdInput by lazy { getInput(REPO_INPUT_ID) }
    private val itemTypeInput by lazy { ChoiceElement(ITEM_TYPE_ID, InputItemType.DEFAULT.value) }
    private val requestTypeInput by lazy { ChoiceElement(REQUEST_TYPE_ID, InputRequestType.DEFAULT.value) }
    private val labelsFilterInput by lazy { getInput(LABELS_FILTER_INPUT_ID) }
    private val statesFilterInput by lazy { getSelector(STATES_FILTER_INPUT_ID) }
    private val limitInput by lazy { getInput(LIMIT_INPUT_ID) }

    fun init(params: URLSearchParams): Boolean {
        val updated = params.get(REPO_QUERY_PARAM)?.let {
            repoIdInput.value = it
            true
        } ?: false

        itemTypeInput.value = params.get(ITEM_QUERY_PARAM) ?: InputItemType.DEFAULT.value
        updateOptions(getItemType())

        requestTypeInput.value = params.get(REQUEST_QUERY_PARAM) ?: InputRequestType.DEFAULT.value

        params.get(INCLUDES_PARAM)?.let {
            labelsFilterInput.value = it
        }

        params.get(STATE_PARAM)?.let {
            statesFilterInput.value = it
        }

        params.get(LIMIT_PARAM)?.let {
            limitInput.value = it
        }

        return updated
    }

    fun getRepositoryId(): RepositoryId? {
        val repoInput = repoIdInput.value

        return RepositoryId.parse(repoInput)
    }

    fun getItemType(): ItemType = InputItemType.forValue(itemTypeInput.value).type

    fun getRequestType(): RequestType = InputRequestType.forValue(requestTypeInput.value).type

    fun getIncludes(): Set<String>? {
        return parseLabels(labelsFilterInput.value)
    }

    fun getStates(): Set<State>? {
        return StateItemType.forValue(statesFilterInput.value).value?.let { setOf(it) }
    }

    fun getLimit(): Int? {
        return limitInput.value.trim().toIntOrNull()?.takeIf { it > 0 }
    }

    fun createShareParams(): String {
        val query = mapOf(
            REPO_QUERY_PARAM to repoIdInput.value,
            ITEM_QUERY_PARAM to itemTypeInput.value,
            REQUEST_QUERY_PARAM to requestTypeInput.value,
            INCLUDES_PARAM to labelsFilterInput.value,
            STATE_PARAM to statesFilterInput.value,
            LIMIT_PARAM to limitInput.value,
        )
        return query.entries.joinToString(prefix = "?", separator = "&") { "${it.key}=${encodeURIComponent(it.value)}" }
    }

    fun updateOptions(itemType: ItemType) {
        val oldValue = statesFilterInput.value
        val options = when (itemType) {
            ItemType.ISSUE -> StateItemType.issueValues
            ItemType.PULL -> StateItemType.pullsValues
        }
        val body = options.map {
            document.create.option {
                selected = (it == StateItemType.ANY)
                value = it.name
                +it.name
            }
        }.toTypedArray()
        statesFilterInput.setContent(*body)
        statesFilterInput.value = oldValue
        if (statesFilterInput.value.isEmpty()) {
            statesFilterInput.value = StateItemType.DEFAULT.name
        }
    }

    private fun parseLabels(input: String): Set<String>? = input
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()
        .takeUnless { it.isEmpty() }

    companion object {
        private const val REPO_INPUT_ID = "repository_id"
        private const val ITEM_TYPE_ID = "item_type"
        private const val REQUEST_TYPE_ID = "request"
        private const val LABELS_FILTER_INPUT_ID = "includes"
        private const val STATES_FILTER_INPUT_ID = "states"
        private const val LIMIT_INPUT_ID = "limit"

        private const val REPO_QUERY_PARAM = "repo"
        private const val ITEM_QUERY_PARAM = "item_type"
        private const val REQUEST_QUERY_PARAM = "request"
        private const val INCLUDES_PARAM = "include"
        private const val STATE_PARAM = "state"
        private const val LIMIT_PARAM = "limit"

        private enum class InputItemType(val value: String, val type: ItemType) {
            ISSUE("issue", ItemType.ISSUE),
            PULL("pull", ItemType.PULL);

            companion object {
                val DEFAULT = PULL
                fun forValue(value: String) = values().single { it.value == value }
            }
        }

        private enum class InputRequestType(val value: String, val type: RequestType) {
            RESOLVE_TIME("resolve_time", RequestType.RESOLVE_TIME),
            LABELS("labels", RequestType.LABELS);

            companion object {
                val DEFAULT = RESOLVE_TIME
                fun forValue(value: String) = values().single { it.value == value }
            }
        }

        private enum class StateItemType(val value: State?) {
            ANY(null),
            OPEN(State.OPEN),
            MERGED(State.MERGED),
            CLOSED(State.CLOSED);

            companion object {
                val DEFAULT = ANY
                val issueValues = setOf(ANY, OPEN, CLOSED)
                val pullsValues = issueValues.plus(MERGED)

                fun forValue(value: String) = values().single { it.name == value }
            }
        }
    }
}


