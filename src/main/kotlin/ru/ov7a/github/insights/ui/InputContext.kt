package ru.ov7a.github.insights.ui

import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.domain.RepositoryId

class InputContext {
    private val repoIdInput by lazy { getTextInput(REPO_INPUT_ID) }

    fun init(params: URLSearchParams): Boolean {
        val updated = params.get(REPO_QUERY_PARAM)?.let {
            repoIdInput.value = it
            true
        } ?: false

        return updated
    }

    fun getRepositoryId(): RepositoryId? {
        val repoInput = repoIdInput.value

        return RepositoryId.parse(repoInput)
    }

    companion object {
        private const val REPO_INPUT_ID = "repository_id"
        private const val REPO_QUERY_PARAM = "repo"
    }
}
