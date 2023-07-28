package ru.ov7a.github.insights

import kotlinx.coroutines.runBlocking

actual fun runTest(block: suspend () -> Unit): Unit = runBlocking {
    block()
}
