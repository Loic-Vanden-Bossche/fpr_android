package fr.imacaron.flashplayerrevival.api

import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.api.resources.Groups
import fr.imacaron.flashplayerrevival.domain.type.GroupType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

class ApiService(private val token: String) {

    private val httpClient = HttpClient {
        install(ContentNegotiation){
            json()
        }
        install(Resources)
        install(WebSockets){
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "192.168.1.63"
                port = 8080
                path("api/")
            }
            bearerAuth(token)
        }
        expectSuccess = true
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                when(exceptionResponse.status){

                }
            }
        }
    }

    val messageChannel: Channel<MessageResponse> = Channel()

    private val writeMessageChannel: Channel<String> = Channel()

    suspend fun write(groupId: UUID, text: String){
        writeMessageChannel.send(text)
    }

    suspend fun initSocket(groupId: UUID){
        println("INIT SOCKET")
        withContext(Dispatchers.IO){
            httpClient.webSocket(method = HttpMethod.Get, host = "192.168.1.63", port = 8080, path = "/socket"){
                send("CONNECT\nAuthorization:$token\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n".encodeToByteArray())
                send(byteArrayOf(0))
                println("Start receiving")
                val frame = incoming.receive() as Frame.Text
                println(frame.readText())
                send("SUBSCRIBE\nid:sub-0\ndestination:/groups/$groupId/messages\n\n")
                send(byteArrayOf(0))
                launch {
                    while(true){
                        val text = writeMessageChannel.receive()
                        send("SEND\ndestination:/app/$groupId/messages\ncontent-length:${text.length}\n\n$text")
                        send(byteArrayOf(0))
                    }
                }
                while(true){
                    println((incoming.receive() as Frame.Text).readText())
                }
            }
        }
    }

    fun closeSocket(){
        httpClient.close()
    }

    val groups: GroupsRoute = GroupsRoute()

    inner class GroupsRoute{
        suspend operator fun invoke(): List<GroupResponse> = httpClient.get(Groups()).body()

        suspend operator fun invoke(id: UUID): Group = Group(httpClient.get(Groups.Id(id = id)).body())

        inner class Group(
            private val original: GroupResponse
        ) {
            val id: UUID get() = UUID.fromString(original.id)
            val name: String get() = original.name
            val type: GroupType get() = original.type
            val members: List<UserResponse> get() = original.members
            suspend fun messages(): List<MessageResponse> = httpClient.get(Groups.Id.Messages(Groups.Id(id = UUID.fromString(original.id)))).body()

            suspend fun connnect(){

            }
        }
    }
}