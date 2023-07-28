package ru.ov7a.github.insights.ui.presentation

private val STDERR = platform.posix.fdopen(2, "w")
fun printlnErr(message: String) {
    printErr("$message\n")
}

fun printErr(message: String) {
    platform.posix.fprintf(STDERR, "%s", message)
    platform.posix.fflush(STDERR)
}
