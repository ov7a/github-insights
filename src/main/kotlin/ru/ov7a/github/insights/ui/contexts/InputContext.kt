package ru.ov7a.github.insights.ui.contexts

import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.domain.ItemType
import ru.ov7a.github.insights.domain.RepositoryId
import ru.ov7a.github.insights.ui.elements.ChoiceElement
import ru.ov7a.github.insights.ui.elements.getInput
import ru.ov7a.github.insights.ui.encodeURIComponent

class InputContext {
    private val repoIdInput by lazy { getInput(REPO_INPUT_ID) }
    private val itemTypeInput by lazy { ChoiceElement(ITEM_TYPE_ID, InputItemType.DEFAULT.value) }

    fun init(params: URLSearchParams): Boolean {
        val updated = params.get(REPO_QUERY_PARAM)?.let {
            repoIdInput.value = it
            true
        } ?: false

        itemTypeInput.value = params.get(ITEM_QUERY_PARAM) ?: InputItemType.DEFAULT.value

        return updated
    }

    fun getRepositoryId(): RepositoryId? {
        val repoInput = repoIdInput.value

        return RepositoryId.parse(repoInput)
    }

    fun getItemType(): ItemType = InputItemType.forValue(itemTypeInput.value).type

    fun createShareParams(): String {
        val query = mapOf(
            REPO_QUERY_PARAM to encodeURIComponent(repoIdInput.value),
            ITEM_QUERY_PARAM to encodeURIComponent(itemTypeInput.value),
        )
        return query.entries.joinToString(prefix = "?", separator = "&") { "${it.key}=${it.value}" }
    }

    companion object {
        private const val REPO_INPUT_ID = "repository_id"
        private const val ITEM_TYPE_ID = "item_type"
        private const val REPO_QUERY_PARAM = "repo"
        private const val ITEM_QUERY_PARAM = "item_type"

        private enum class InputItemType(val value: String, val type: ItemType) {
            ISSUE("issue", ItemType.ISSUE),
            PULL("pull", ItemType.PULL);

            companion object {
                val DEFAULT = PULL
                fun forValue(value: String) = values().single { it.value == value }
            }
        }
    }
}


