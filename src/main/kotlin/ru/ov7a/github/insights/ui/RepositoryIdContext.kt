package ru.ov7a.github.insights.ui

import ru.ov7a.github.insights.domain.RepositoryId

class RepositoryIdContext {
    private val repoIdInput by lazy { getTextInput(REPO_INPUT_ID) }

    fun setRepositoryValue(newValue: String) {
        repoIdInput.value = newValue
    }

    fun getRepositoryId(): RepositoryId? {
        val repoInput = repoIdInput.value

        return RepositoryId.parse(repoInput)
    }

    companion object {
        private const val REPO_INPUT_ID = "repository_id"
        const val REPO_QUERY_PARAM = "repo"
    }
}
