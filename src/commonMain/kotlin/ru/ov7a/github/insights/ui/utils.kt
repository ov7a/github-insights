package ru.ov7a.github.insights.ui

import io.ktor.client.plugins.ClientRequestException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import ru.ov7a.github.insights.fetcher.graphql.GraphQLError

@ExperimentalTime
fun humanReadableDuration(duration: Duration): String {
    val truncated = duration.toComponents { days, hours, minutes, seconds, _ ->
        when {
            days > 7 -> days.days
            days > 0 -> days.days + hours.hours
            hours > 0 -> hours.hours + minutes.minutes
            else -> minutes.minutes + seconds.seconds
        }
    }
    return truncated.toString()
}

fun parseStringsSet(input: String): Set<String>? = input
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .toSet()
    .takeUnless { it.isEmpty() }

fun extractErrorMessage(exception: Throwable?) = when (exception) {
    is ClientRequestException -> "Error during fetching data: ${exception.response.status.description}"
    is GraphQLError -> "Error during fetching data: ${exception.message}"
    else -> "Error happened: ${exception?.message ?: "Unknown error"} ${exception?.stackTraceToString()}"
}
