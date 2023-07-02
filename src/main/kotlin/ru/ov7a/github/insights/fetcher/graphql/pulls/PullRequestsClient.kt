package ru.ov7a.github.insights.fetcher.graphql.pulls

import ru.ov7a.github.insights.domain.IssueLike
import ru.ov7a.github.insights.fetcher.JsonClient
import ru.ov7a.github.insights.fetcher.graphql.AbstractGraphQLClient

class PullRequestsClient(jsonClient: JsonClient) : AbstractGraphQLClient<PullRequestResponse>(jsonClient) {
    override val name = "pullRequests"
    override fun getRequestFields(): String = "url, createdAt, mergedAt, closedAt"

    override fun PullRequestResponse.convert() = IssueLike(
        url = url,
        createdAt = createdAt,
        closedAt = closedAt ?: mergedAt,
    )
}
