package ru.ov7a.pull_requests.calculation

import ru.ov7a.pull_requests.domain.PullRequestsBatch

open class ProgressReporter {

    open suspend fun report(value: Double) {
        //by default, do nothing
    }

    var count: Int = 0
        private set

    suspend fun consume(pullRequestsBatch: PullRequestsBatch) {
        count += pullRequestsBatch.pullRequests.size
        if (pullRequestsBatch.totalCount != 0) {
            report(count.toDouble() / pullRequestsBatch.totalCount)
        } else {
            report(1.0)
        }
    }

}