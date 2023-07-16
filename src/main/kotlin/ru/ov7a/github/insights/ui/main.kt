package ru.ov7a.github.insights.ui

import getAndCalculate
import kotlin.time.ExperimentalTime
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.url.URLSearchParams
import ru.ov7a.github.insights.calculation.labels.calculateLabelsGraph
import ru.ov7a.github.insights.calculation.stats.calculateResolveTime
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.Filters
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.RequestType
import ru.ov7a.github.insights.ui.elements.ProgressBarReporter
import ru.ov7a.github.insights.ui.presentation.LabelsPresenter
import ru.ov7a.github.insights.ui.presentation.Presenter
import ru.ov7a.github.insights.ui.presentation.StatsPresenter

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
@OptIn(ExperimentalJsExport::class, ExperimentalTime::class)
fun calculateAndPresent() = catchValidationError {
    val repositoryId = context.inputs.getRepositoryId() ?: throw ValidationException(
        "Can't parse input. Please, provide it as url to repository or as %user%/%repositoryName%"
    )
    val authorization = context.authorization.getAuthorization() ?: throw ValidationException(
        "Please, authorize"
    )
    val requestType = context.inputs.getRequestType()
    val filters = Filters(
        includeLabels = context.inputs.getIncludes(),
        states = context.inputs.getStates(),
        limit = context.inputs.getLimit(),
    )
    val fetchParameters = FetchParameters(
        context.inputs.getItemType(),
        repositoryId,
        filters,
        authorization
    )
    context.presentation.setLoading()

    when (requestType) {
        RequestType.RESOLVE_TIME -> calculateAndPresent(fetchParameters, ::calculateResolveTime, StatsPresenter)
        RequestType.LABELS -> calculateAndPresent(fetchParameters, ::calculateLabelsGraph, LabelsPresenter)
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun <Data : Any> calculateAndPresent(
    fetchParameters: FetchParameters,
    calculator: suspend (Flow<IssueLike>) -> Data?,
    presenter: Presenter<Data>,
) {
    GlobalScope.launch {
        val reporter = ProgressBarReporter()

        val result = getAndCalculate(fetchParameters, reporter, calculator = calculator)

        context.presentation.present(fetchParameters, result, presenter)
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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun updateType() {
    val itemType = context.inputs.getItemType()
    context.inputs.updateOptions(itemType)
}
