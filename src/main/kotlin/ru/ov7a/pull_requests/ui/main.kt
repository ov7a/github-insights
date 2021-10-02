package ru.ov7a.pull_requests.ui

import getAndCalculateStats
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import ru.ov7a.pull_requests.ui.RepositoryIdContext.Companion.REPO_QUERY_PARAM
import kotlin.time.ExperimentalTime

private val context = Context()

@OptIn(DelicateCoroutinesApi::class)
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
@OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
fun calculateAndPresent() = catchValidationError {
    val repositoryId = context.repositoryId.getRepositoryId() ?: throw ValidationException(
        "Can't parse input. Please, provide it as url to repository or as %user%/%repositoryName%"
    )

    context.presentation.setLoading()

    GlobalScope.launch {
        val reporter = ProgressBarReporter()

        val result = getAndCalculateStats(
            context.pullRequestsClient,
            repositoryId,
            context.authorization.getAuthorization(),
            reporter
        )

        context.presentation.present(result)
    }
}

@JsExport
fun storeAuthorization() = catchValidationError {
    context.authorization.saveAuthorization()
}

@JsExport
fun resetAuthorization() {
    context.authorization.resetAuthorization()
}
