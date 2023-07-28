package ru.ov7a.github.insights.ui.contexts

import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.span
import ru.ov7a.github.insights.domain.FetchParameters
import ru.ov7a.github.insights.ui.elements.getHtmlElement
import ru.ov7a.github.insights.ui.elements.hide
import ru.ov7a.github.insights.ui.elements.setContent
import ru.ov7a.github.insights.ui.elements.show
import ru.ov7a.github.insights.ui.extractErrorMessage
import ru.ov7a.github.insights.ui.presentation.Presenter

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

    fun <Data : Any> present(
        fetchParameters: FetchParameters,
        result: Result<Data?>,
        dataPresenter: Presenter<Data>
    ) {
        progressBarBlock.hide()

        when {
            result.isSuccess -> presentSuccess(fetchParameters, result.getOrNull(), dataPresenter)
            result.isFailure -> presentFailure(result.exceptionOrNull())
        }
    }

    private fun <Data : Any> presentSuccess(
        fetchParameters: FetchParameters,
        data: Data?,
        dataPresenter: Presenter<Data>
    ) {
        if (data != null) {
            successResultBlock.apply {
                setContent(dataPresenter.render(fetchParameters, data))
                show()
            }
        } else {
            noDataResultBlock.show()
        }
    }

    private fun presentFailure(exception: Throwable?) {
        failureResultBlock.apply {
            setContent(
                document.create.span {
                    +extractErrorMessage(exception)
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
