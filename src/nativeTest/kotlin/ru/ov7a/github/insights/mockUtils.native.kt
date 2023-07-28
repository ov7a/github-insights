package ru.ov7a.github.insights

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

actual fun loadResource(resource: String): String {
    FileSystem.SYSTEM.source("src/commonTest/resources/$resource".toPath()).use {
        return it.buffer().readUtf8()
    }
}
