package ru.ov7a.github.insights.fetcher

import io.ktor.util.encodeBase64
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

//ported from io.ktor.client.features.auth.providers.BasicAuthProvider
fun constructBasicAuthValue(user: String, password: String): String {
    val authEncoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()

    return "Basic $authEncoded"
}
