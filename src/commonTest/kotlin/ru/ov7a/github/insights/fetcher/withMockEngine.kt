package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler

suspend fun <T> withMockEngine(
    createClient: (JsonClient) -> Client,
    vararg handlers: MockRequestHandler,
    action: suspend Client.() -> T
): T {
    val client = createClientWithMocks(createClient, *handlers)
    return client.action()
}

fun createClientWithMocks(
    createClient: (JsonClient) -> Client,
    vararg handlers: MockRequestHandler,
): Client {
    val engine = MockEngine(MockEngineConfig().apply { requestHandlers.addAll(handlers) })
    val jsonClient = JsonClient { block -> HttpClient(engine, block) }
    return createClient(jsonClient)
}
