package ru.ov7a.pull_requests.fetcher.graphql

import kotlinx.serialization.Serializable
import ru.ov7a.pull_requests.domain.RepositoryId

@Serializable
data class GraphQLRequest(
    val query: String
)

fun createPullRequestsQuery(
    repositoryId: RepositoryId,
    cursor: String?
): GraphQLRequest {
    return GraphQLRequest(
        """{
  repository(name: "${repositoryId.name}", owner: "${repositoryId.owner}") {
    pullRequests(last: 100${cursor?.let { """, before: "$it"""" } ?: ""}) {
      totalCount
      nodes { url, createdAt, mergedAt, updatedAt, closedAt, state }
      pageInfo { startCursor, hasPreviousPage }
    }
  }
}"""
    )
}

