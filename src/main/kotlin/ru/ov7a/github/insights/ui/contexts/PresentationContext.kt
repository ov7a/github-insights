package ru.ov7a.github.insights.ui.contexts

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
import ru.ov7a.github.insights.domain.ItemType
import ru.ov7a.github.insights.domain.Stats
import ru.ov7a.github.insights.fetcher.graphql.GraphQLError
import ru.ov7a.github.insights.ui.elements.getHtmlElement
import ru.ov7a.github.insights.ui.elements.hide
import ru.ov7a.github.insights.ui.elements.setContent
import ru.ov7a.github.insights.ui.elements.show
import ru.ov7a.github.insights.ui.humanReadableDuration

@OptIn(ExperimentalTime::class)
class PresentationContext {
    private val progressBarBlock by lazy { getHtmlElement(LOADING_BLOCK_ID) }
    private val successResultBlock by lazy { getHtmlElement(SUCCESS_RESULT_BLOCK_ID) }
    private val noDataResultBlock by lazy { getHtmlElement(NO_DATA_RESULT_BLOCK_ID) }
    private val failureResultBlock by lazy { getHtmlElement(FAILURE_RESULT_BLOCK_ID) }
    private val helpHint by lazy { getHtmlElement(HELP_HINT_ID) }

    fun setLoading() {
        progressBarBlock.show()
        successResultBlock.hide()
        noDataResultBlock.hide()
        failureResultBlock.hide()
    }

    fun present(result: Result<Stats?>) {
        progressBarBlock.hide()

        when {
            result.isSuccess -> presentSuccess(result.getOrNull())
            result.isFailure -> presentFailure(result.exceptionOrNull())
        }
    }

    fun updateHint(itemType: ItemType) {
        val text = when (itemType) {
            ItemType.PULL -> "How long will your pull request be reviewed? Get an estimate!"
            ItemType.ISSUE -> "How long should you wait until your issue is resolved? Get an estimate!"
        }
        helpHint.textContent = text
    }

    private fun presentSuccess(stats: Stats?) {
        if (stats != null) {
            successResultBlock.apply {
                setContent(generateResultsHtml(stats))
                show()
            }
        } else {
            noDataResultBlock.show()
        }
    }

    private fun generateResultsHtml(stats: Stats): HTMLTableElement =
        document.create.table {
            tbody {
                stats.map { stat ->
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
                        is ClientRequestException -> "Error during fetching data: ${exception.response.status.description}"
                        is GraphQLError -> "Error during fetching data: ${exception.message}"
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
        const val HELP_HINT_ID = "help"
    }
}



