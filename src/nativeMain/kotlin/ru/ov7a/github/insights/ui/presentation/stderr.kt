package ru.ov7a.github.insights.ui.presentation

import kotlin.system.exitProcess

private val STDERR = platform.posix.fdopen(2, "w")
fun printlnErr(message: String) {
    printErr("$message\n")
}

fun printErr(message: String) {
    platform.posix.fprintf(STDERR, "%s", message)
    platform.posix.fflush(STDERR)
}

fun fail(message: String): Nothing {
    printlnErr("")
    printlnErr(message)
    exitProcess(1)
}
