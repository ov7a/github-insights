package ru.ov7a.github.insights

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

@OptIn(DelicateCoroutinesApi::class)
fun runTest(block: suspend (scope: CoroutineScope) -> Unit): dynamic = GlobalScope.promise { block(this) }
