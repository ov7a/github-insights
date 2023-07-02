package ru.ov7a.github.insights.ui

import io.ktor.client.plugins.ClientRequestException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.span
import kotlinx.html.js.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tr
import org.w3c.dom.HTMLTableElement
import ru.ov7a.github.insights.domain.PullRequestsStats

@OptIn(ExperimentalTime::class)
class PresentationContext {
    private val progressBarBlock by lazy { getHtmlElement(LOADING_BLOCK_ID) }
    private val successResultBlock by lazy { getHtmlElement(SUCCESS_RESULT_BLOCK_ID) }
    private val noDataResultBlock by lazy { getHtmlElement(NO_DATA_RESULT_BLOCK_ID) }
    private val failureResultBlock by lazy { getHtmlElement(FAILURE_RESULT_BLOCK_ID) }

    fun setLoading() {
        progressBarBlock.show()
        successResultBlock.hide()
        noDataResultBlock.hide()
        failureResultBlock.hide()
    }

    fun present(result: Result<PullRequestsStats?>) {
        progressBarBlock.hide()

        when {
            result.isSuccess -> presentSuccess(result.getOrNull())
            result.isFailure -> presentFailure(result.exceptionOrNull())
        }
    }

    private fun presentSuccess(pullRequestsStats: PullRequestsStats?) {
        if (pullRequestsStats != null) {
            successResultBlock.apply {
                setContent(generateResultsHtml(pullRequestsStats))
                show()
            }
        } else {
            noDataResultBlock.show()
        }
    }

    private fun generateResultsHtml(pullRequestsStats: PullRequestsStats): HTMLTableElement =
        document.create.table {
            tbody {
                pullRequestsStats.map { stat ->
                    tr {
                        td { +stat.displayName }
                        td {
                            +when (stat.value) {
                                is Duration -> humanReadableDuration(stat.value)
                                else -> stat.value.toString()
                            }
                        }
                    }
                }
            }
        }

    private fun presentFailure(exception: Throwable?) {
        failureResultBlock.apply {
            setContent(
                document.create.span {
                    +when (exception) {
                        is ClientRequestException -> "Error during fetching pull requests data: ${exception.response.status.description}"
                        else -> "Error happened: ${exception?.message ?: "Unknown error"}"
                    }
                }
            )
            show()
        }
    }

    private companion object {
        const val LOADING_BLOCK_ID = "loading"
        const val SUCCESS_RESULT_BLOCK_ID = "results_success"
        const val NO_DATA_RESULT_BLOCK_ID = "results_no_data"
        const val FAILURE_RESULT_BLOCK_ID = "results_error"
    }
}



