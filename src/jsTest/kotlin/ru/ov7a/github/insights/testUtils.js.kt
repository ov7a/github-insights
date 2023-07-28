package ru.ov7a.github.insights

import io.kotest.common.runPromise

actual fun runTest(block: suspend () -> Unit): Unit = runPromise {
    block()
}
