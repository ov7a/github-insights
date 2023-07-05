package ru.ov7a.github.insights.ui

import getAndCalculateStats
import kotlin.time.ExperimentalTime
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.ui.elements.ProgressBarReporter

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
    val updated = context.inputs.init(queryParams)

    if (updated && context.authorization.getAuthorization() != null) {
        calculateAndPresent()
    }
}

@JsExport
@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class, ExperimentalTime::class)
fun calculateAndPresent() = catchValidationError {
    val repositoryId = context.inputs.getRepositoryId() ?: throw ValidationException(
        "Can't parse input. Please, provide it as url to repository or as %user%/%repositoryName%"
    )
    val authorization = context.authorization.getAuthorization() ?: throw ValidationException(
        "Please, authorize"
    )
    val fetchParameters = FetchParameters(
        context.inputs.getItemType(),
        repositoryId,
        authorization
    )
    context.presentation.setLoading()

    GlobalScope.launch {
        val reporter = ProgressBarReporter()

        val result = getAndCalculateStats(fetchParameters, reporter)

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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun copyShareLink() {
    val location = window.location.href
    val params = context.inputs.createShareParams()
    window.navigator.clipboard.writeText(location.substringBefore("?") + params)
    window.alert("Link copied to clipboard")
}
