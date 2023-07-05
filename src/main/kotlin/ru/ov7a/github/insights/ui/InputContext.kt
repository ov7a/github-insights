package ru.ov7a.github.insights.ui

import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.domain.RepositoryId

class InputContext {
    private val repoIdInput by lazy { getInput(REPO_INPUT_ID) }
    private val itemTypeInput by lazy { ChoiceElement(ITEM_TYPE_ID, ItemType.DEFAULT.value) }

    fun init(params: URLSearchParams): Boolean {
        val updated = params.get(REPO_QUERY_PARAM)?.let {
            repoIdInput.value = it
            true
        } ?: false

        itemTypeInput.value = params.get(ITEM_QUERY_PARAM) ?: ItemType.DEFAULT.value

        return updated
    }

    fun getRepositoryId(): RepositoryId? {
        val repoInput = repoIdInput.value

        return RepositoryId.parse(repoInput)
    }

    fun getItemType(): ItemType = ItemType.forValue(itemTypeInput.value)

    companion object {
        private const val REPO_INPUT_ID = "repository_id"
        private const val ITEM_TYPE_ID = "item_type"
        private const val REPO_QUERY_PARAM = "repo"
        private const val ITEM_QUERY_PARAM = "item_type"
    }
}

enum class ItemType(val value: String) {
    ISSUE("issue"),
    PULL("pull");

    companion object {
        val DEFAULT = PULL
        fun forValue(value: String) = values().single { it.value == value }
    }
}
