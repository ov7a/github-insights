package ru.ov7a.github.insights.ui.presentation

import ru.ov7a.github.insights.domain.FetchParameters

interface Presenter<Data : Any> {
    fun print(fetchParameters: FetchParameters, data: Data): Unit
}
