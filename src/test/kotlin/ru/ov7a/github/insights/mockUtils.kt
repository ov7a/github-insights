package ru.ov7a.github.insights

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import org.w3c.xhr.XMLHttpRequest

data class Endpoint(
    val url: Url,
    val method: HttpMethod = HttpMethod.Get,
    val headers: Headers = headersOf(),
) {
    @OptIn(InternalAPI::class)
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

@OptIn(InternalAPI::class)
fun response(
    content: String,
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    headers: Map<String, List<String>> = emptyMap(),
): MockRequestHandler = {
    respond(content, statusCode, HeadersImpl(headers))
}

fun loadResource(resource: String): String {
    XMLHttpRequest().apply {
        open("GET", "/base/$resource", async = false)
        send()
        return responseText
    }
}
