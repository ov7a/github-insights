package ru.ov7a.github.insights.fetcher.graphql.issues

import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.fetcher.JsonClient
import ru.ov7a.github.insights.fetcher.graphql.AbstractGraphQLClient
import ru.ov7a.github.insights.fetcher.graphql.RepositoryResponse

class IssuesClient(jsonClient: JsonClient) : AbstractGraphQLClient<IssueResponse>(jsonClient) {
    override val name = "issues"

    override val customFields = ""

    override val responseField = RepositoryResponse::issues

    override fun IssueResponse.convert() = IssueLike(
        url = url,
        createdAt = createdAt,
        closedAt = closedAt,
        labels = labels.convert(),
        comments = comments.totalCount,
        reactions = reactions.totalCount,
    )
}
