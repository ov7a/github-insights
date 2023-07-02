package ru.ov7a.github.insights.ui

import getAndCalculateStats
import kotlin.time.ExperimentalTime
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.ui.RepositoryIdContext.Companion.REPO_QUERY_PARAM

private val context = Context()

fun main() {
    window.onerror = { message, _, _, _, _ ->
        window.alert(message)
        console.error(message)
        true
    }
    window.onload = { init() }
}

fun init() {
    context.authorization.init()

    val queryParams = window.location.search.let {
        URLSearchParams(it)
    }
    val repoParam = queryParams.get(REPO_QUERY_PARAM)

    repoParam?.let {
        context.repositoryId.setRepositoryValue(it)
        calculateAndPresent()
    }
}

@JsExport
@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class, ExperimentalTime::class)
fun calculateAndPresent() = catchValidationError {
    val repositoryId = context.repositoryId.getRepositoryId() ?: throw ValidationException(
        "Can't parse input. Please, provide it as url to repository or as %user%/%repositoryName%"
    )
    val authorization = context.authorization.getAuthorization() ?: throw ValidationException(
        "Please, authorize"
    )

    context.presentation.setLoading()

    GlobalScope.launch {
        val reporter = ProgressBarReporter()

        val result = getAndCalculateStats(
            context.pullRequestsClient,
            repositoryId,
            authorization,
            reporter
        )

        context.presentation.present(result)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun storeAuthorization() = catchValidationError {
    context.authorization.saveAuthorization()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun resetAuthorization() {
    context.authorization.resetAuthorization()
}
