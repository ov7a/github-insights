package ru.ov7a.github.insights.ui.input

import kotlinx.cinterop.toKString
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import platform.posix.getenv
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.Filters
import ru.ov7a.github.insights.domain.State
import ru.ov7a.github.insights.domain.input.ItemType
import ru.ov7a.github.insights.domain.input.RepositoryId
import ru.ov7a.github.insights.domain.input.RequestType
import ru.ov7a.github.insights.ui.parseStringsSet

private const val TOKEN_ENV = "GITHUB_TOKEN"
fun parseArguments(args: Array<String>): Pair<FetchParameters, RequestType> {
    val token = getenv(TOKEN_ENV)?.toKString() ?: throw Exception(
        """
        Please provide GitHub token as $TOKEN_ENV environment variable.
        Get your token here: https://github.com/settings/tokens
        No additional scopes are needed.
        """.trimIndent()
    )

    val parser = ArgParser("github-insights")

    val itemType by parser.argument(ArgType.Choice<ItemType>())
    val requestType by parser.argument(ArgType.Choice<RequestType>())
    val repositoryId by parser.argument(ArgType.String, description = "repository, e.g. torvalds/linux")

    val limit by parser.option(ArgType.Int)
    val states by parser.option(ArgType.String, description = "states, comma-separated")
    val includeLabels by parser.option(ArgType.String, description = "labels, comma-separated")

    parser.parse(args)

    val auth = "Bearer $token"

    val repo = RepositoryId.parse(repositoryId)
        ?: throw Exception("Can't parse repository. Please, provide it as url to repository or as %user%/%repositoryName%")

    val statesFilter = states?.let(::parseStringsSet)?.map { State.valueOf(it) }?.toSet()
    val labelsFilter = includeLabels?.let(::parseStringsSet)

    val fetchParameters = FetchParameters(
        itemType,
        repo,
        Filters(limit, statesFilter, labelsFilter),
        auth
    )
    return fetchParameters to requestType
}
