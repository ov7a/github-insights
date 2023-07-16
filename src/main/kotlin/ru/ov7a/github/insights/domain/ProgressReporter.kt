package ru.ov7a.github.insights.domain

import ru.ov7a.github.insights.domain.input.DataBatch

open class ProgressReporter {

    open suspend fun report(value: Double) {
        // by default, do nothing
    }

    var count: Int = 0
        private set

    suspend fun consume(dataBatch: DataBatch) {
        count += dataBatch.data.size
        if (dataBatch.totalCount != 0) {
            report(count.toDouble() / dataBatch.totalCount)
        } else {
            report(1.0)
        }
    }
}