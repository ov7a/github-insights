package ru.ov7a.github.insights

import getAndCalculate
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import ru.ov7a.github.insights.calculation.labels.calculateLabelsGraph
import ru.ov7a.github.insights.calculation.stats.calculateResolveTime
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.domain.ProgressReporter
import ru.ov7a.github.insights.domain.input.IssueLike
import ru.ov7a.github.insights.domain.input.RequestType
import ru.ov7a.github.insights.ui.extractErrorMessage
import ru.ov7a.github.insights.ui.input.parseArguments
import ru.ov7a.github.insights.ui.presentation.LabelsPresenter
import ru.ov7a.github.insights.ui.presentation.Presenter
import ru.ov7a.github.insights.ui.presentation.StatsPresenter

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) = runBlocking {
    val (fetchParameters, requestType) = parseArguments(args)

    when (requestType) {
        RequestType.RESOLVE_TIME -> calculateAndPresent(fetchParameters, ::calculateResolveTime, StatsPresenter)
        RequestType.LABELS -> calculateAndPresent(fetchParameters, ::calculateLabelsGraph, LabelsPresenter)
    }
}

private suspend fun <Data : Any> calculateAndPresent(
    fetchParameters: FetchParameters,
    calculator: suspend (Flow<IssueLike>) -> Data?,
    presenter: Presenter<Data>,
) {
    val reporter = ProgressReporter() // TODO

    val result = getAndCalculate(fetchParameters, reporter, calculator = calculator)

    present(fetchParameters, result, presenter)
}

private fun <Data : Any> present(
    fetchParameters: FetchParameters,
    result: Result<Data?>,
    dataPresenter: Presenter<Data>
) {
    when {
        result.isSuccess -> {
            val data = result.getOrNull()
            if (data != null) {
                dataPresenter.print(fetchParameters, data)
            } else {
                println("No matching results were found in this repository")
            }
        }

        result.isFailure -> println(extractErrorMessage(result.exceptionOrNull()))
    }
}
