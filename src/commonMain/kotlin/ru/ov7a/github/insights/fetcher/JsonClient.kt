package ru.ov7a.github.insights.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import ru.ov7a.github.insights.fetcher.graphql.RepositoryResponse
import ru.ov7a.github.insights.fetcher.graphql.issues.IssuesRepositoryResponse
import ru.ov7a.github.insights.fetcher.graphql.pulls.PullsRepositoryResponse

class JsonClient(
    clientBuilder: (HttpClientConfig<*>.() -> Unit) -> HttpClient = ::HttpClient,
) {
    val client: HttpClient = clientBuilder {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                // kotlinx.serialization, srsly, is where a better way?
                serializersModule = SerializersModule {
                    polymorphicDefaultDeserializer(RepositoryResponse::class) {
                        object : DeserializationStrategy<RepositoryResponse<out Any>> {
                            override val descriptor = buildClassSerialDescriptor("RepositoryResponse")

                            override fun deserialize(decoder: Decoder): RepositoryResponse<out Any> {
                                val jsonDecoder =
                                    decoder as? JsonDecoder ?: error("This serializer can only be used with JSON")
                                val jsonElement = jsonDecoder.decodeJsonElement().jsonObject

                                val deserializer = when {
                                    IssuesRepositoryResponse::issues.name in jsonElement -> IssuesRepositoryResponse.serializer()
                                    PullsRepositoryResponse::pullRequests.name in jsonElement -> PullsRepositoryResponse.serializer()
                                    else -> error("Unknown response type")
                                }
                                return jsonDecoder.json.decodeFromJsonElement(deserializer, jsonElement)
                            }
                        }
                    }
                }
            })
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }
}
