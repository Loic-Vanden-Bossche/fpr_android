package fr.imacaron.flashplayerrevival.data.api

import fr.imacaron.flashplayerrevival.data.error.ConflictingUser
import fr.imacaron.flashplayerrevival.data.error.InvalidField
import fr.imacaron.flashplayerrevival.data.error.LoginError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.util.network.*
import io.ktor.utils.io.*

internal object ApiService{
    const val HOST = "api.flash-player-revival.net"
    val PROTOCOL = URLProtocol.HTTPS
    const val PORT = 443
//        const val HOST = "192.168.1.63"


    var token: String = ""

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        defaultRequest {
            url {
                protocol = PROTOCOL
                host = HOST
                port = PORT
                path("api/")
            }
            if(token != ""){
                bearerAuth(token)
            }
        }
        expectSuccess = true
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                when(exceptionResponse.status){
                    HttpStatusCode.Unauthorized -> throw LoginError()
                    HttpStatusCode.BadRequest -> throw InvalidField()
                    HttpStatusCode.Conflict -> throw ConflictingUser()
                }
            }
        }
    }

    private suspend inline fun <reified T: Any, reified R>request(resource: T, method: HttpMethod): R = request<T, R, Any?>(resource, method, null)

    private suspend inline fun <reified T: Any, reified R, reified B>request(resource: T, method: HttpMethod, body: B?): R =
        try {
            httpClient.request(resource) {
                contentType(ContentType.Application.Json)
                body?.let {
                    setBody(it)
                }
                this.method = method
            }.body()
        }catch (_: UnresolvedAddressException){
            throw NoInternetException()
        }

    suspend inline fun <reified T: Any, reified R> get(resource: T): R = request(resource, HttpMethod.Get)

    suspend inline fun <reified T: Any, reified R, reified B> post(resource: T, body: B): R = request(resource, HttpMethod.Post, body)

    @OptIn(InternalAPI::class)
    suspend inline fun <reified T: Any, reified R> post(resource: T, data: ByteArray, type: ContentType): R =
        httpClient.post {
            body = StreamContent(data, type)
        }.body()

    suspend inline fun <reified T: Any, reified R, reified B> put(resource: T, body: B): R = request(resource, HttpMethod.Put, body)

    suspend inline fun <reified T: Any, reified R, reified B> patch(resource: T, body: B): R = request(resource, HttpMethod.Patch, body)

    suspend inline fun <reified T: Any, reified R> delete(resource: T): R = request(resource, HttpMethod.Delete)

    class StreamContent(private val input: ByteArray, override val contentType: ContentType) : OutgoingContent.WriteChannelContent() {
        override suspend fun writeTo(channel: ByteWriteChannel) {
            channel.writeAvailable(input)
        }

        override val contentLength: Long = input.size.toLong()
    }
}