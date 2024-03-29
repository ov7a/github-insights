package ru.ov7a.github.insights.fetcher.graphql.pulls

import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.fetcher.JsonClient
import ru.ov7a.github.insights.fetcher.graphql.AbstractGraphQLClient
import ru.ov7a.github.insights.fetcher.graphql.RepositoryResponse

class PullRequestsClient(jsonClient: JsonClient) : AbstractGraphQLClient<PullRequestResponse>(jsonClient) {
    override val name = "pullRequests"

    override val customFields = "mergedAt"

    override val responseField = RepositoryResponse::pullRequests

    override fun PullRequestResponse.convert() = IssueLike(
        url = url,
        createdAt = createdAt,
        closedAt = closedAt ?: mergedAt,
        labels = labels.convert(),
        comments = comments.totalCount,
        reactions = reactions.totalCount,
    )
}
