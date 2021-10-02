package ru.ov7a.pull_requests.fetcher

import io.ktor.util.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*

//ported from io.ktor.client.features.auth.providers.BasicAuthProvider
@OptIn(InternalAPI::class)
fun constructBasicAuthValue(user: String, password: String): String {
    val authEncoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()

    return "Basic $authEncoded"
}