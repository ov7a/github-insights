package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler

suspend fun <T> withMockEngine(
    vararg handlers: MockRequestHandler,
    action: suspend PullRequestsClient.() -> T
): T {
    val client = createClientWithMocks(*handlers)
    return client.action()
}

fun createClientWithMocks(
    vararg handlers: MockRequestHandler
): PullRequestsClient {
    val engine = MockEngine(MockEngineConfig().apply { requestHandlers.addAll(handlers) })
    return PullRequestsClient { block -> HttpClient(engine, block) }
}
