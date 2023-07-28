package ru.ov7a.github.insights

import org.w3c.xhr.XMLHttpRequest

actual fun loadResource(resource: String): String {
    XMLHttpRequest().apply {
        open("GET", "/base/$resource", async = false)
        send()
        return responseText
    }
}
