package ru.ov7a.github.insights

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import io.ktor.http.HeadersImpl
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.http.takeFrom

data class Endpoint(
    val url: Url,
    val method: HttpMethod = HttpMethod.Get,
    val headers: Headers = headersOf(),
) {
    constructor(
        url: String,
        method: HttpMethod = HttpMethod.Get,
        headers: Map<String, List<String>> = emptyMap(),
    ) : this(
        URLBuilder().takeFrom(url).build(),
        method,
        HeadersImpl(headers),
    )
}

fun mockResponse(
    endpoint: Endpoint,
    expectedBody: String,
    response: MockRequestHandler
): MockRequestHandler = mockResponse(
    endpoint,
    { (it as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString() shouldBe expectedBody },
    response
)

fun mockResponse(
    endpoint: Endpoint,
    response: MockRequestHandler
): MockRequestHandler = mockResponse(endpoint, { }, response)

fun mockResponse(
    endpoint: Endpoint,
    bodyValidator: (OutgoingContent) -> Unit = { },
    response: MockRequestHandler
): MockRequestHandler {
    return { request ->
        val requestEndpoint = Endpoint(
            url = request.url,
            method = request.method,
            headers = request.headers
        )

        requestEndpoint shouldBe endpoint

        bodyValidator(request.body)

        response(request)
    }
}

fun response(
    content: String,
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    headers: Map<String, List<String>> = emptyMap(),
): MockRequestHandler = {
    respond(content, statusCode, HeadersImpl(headers))
}

expect fun loadResource(resource: String): String
