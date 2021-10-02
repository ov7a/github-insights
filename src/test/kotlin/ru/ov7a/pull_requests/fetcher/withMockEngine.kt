package ru.ov7a.pull_requests.fetcher

import io.ktor.client.*
import io.ktor.client.engine.mock.*

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