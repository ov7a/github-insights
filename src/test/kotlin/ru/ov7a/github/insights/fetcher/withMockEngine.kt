package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import ru.ov7a.github.insights.fetcher.graphql.pulls.PullRequestsClient

suspend fun <T> withMockEngine(
    vararg handlers: MockRequestHandler,
    action: suspend Client.() -> T
): T {
    val client = createClientWithMocks(*handlers)
    return client.action()
}

fun createClientWithMocks(
    vararg handlers: MockRequestHandler
): Client {
    val engine = MockEngine(MockEngineConfig().apply { requestHandlers.addAll(handlers) })
    val jsonClient = JsonClient { block -> HttpClient(engine, block) }
    return PullRequestsClient(jsonClient)
}
