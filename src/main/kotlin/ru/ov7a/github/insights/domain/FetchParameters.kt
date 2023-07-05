package ru.ov7a.github.insights.domain

data class FetchParameters(
    val itemType: ItemType,
    val repositoryId: RepositoryId,
    val authorizationHeader: String,
)
