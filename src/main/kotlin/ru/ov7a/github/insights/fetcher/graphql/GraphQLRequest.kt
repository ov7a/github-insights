package ru.ov7a.github.insights.fetcher.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLRequest(
    val query: String
)
