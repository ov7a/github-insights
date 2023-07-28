package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class JsonClient(
    clientBuilder: (HttpClientConfig<*>.() -> Unit) -> HttpClient = ::HttpClient,
) {
    val client: HttpClient = clientBuilder {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }
}
