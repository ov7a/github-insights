package ru.ov7a.github.insights.domain

import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.domain.input.RepositoryId

data class FetchParameters(
    val itemType: ItemType,
    val repositoryId: RepositoryId,
    val filters: Filters,
    val authorizationHeader: String,
)

data class Filters(
    val limit: Int? = null,
    val states: Set<State>? = null,
    val includeLabels: Set<String>? = null,
)

enum class State {
    OPEN,
    CLOSED,
    MERGED,
}