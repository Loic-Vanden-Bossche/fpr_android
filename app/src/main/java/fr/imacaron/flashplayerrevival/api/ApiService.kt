package fr.imacaron.flashplayerrevival.api

import fr.imacaron.flashplayerrevival.api.dto.`in`.SendMessage
import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.api.resources.Groups
import fr.imacaron.flashplayerrevival.api.type.STOMPMethod
import fr.imacaron.flashplayerrevival.api.type.WriteMessage
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ApiService(private val token: String) {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        install(WebSockets) {
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
                when (exceptionResponse.status) {

                }
            }
        }
    }

    private val messageChannel: Channel<MessageResponse> = Channel()

    val messageFlow: Flow<MessageResponse> = flow {
        while(true){
            val data = messageChannel.receive()
            emit(data)
        }
    }

    private val writeMessageChannel: Channel<WriteMessage> = Channel()

    private val EOF: ByteArray = byteArrayOf(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun initSocket(){
        println("INIT SOCKET")
        withContext(Dispatchers.IO){
            httpClient.webSocket(method = HttpMethod.Get, host = "192.168.1.63", port = 8080, path = "/socket"){
                send("CONNECT\nAuthorization:$token\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n".encodeToByteArray())
                send(byteArrayOf(0))
                val frame = incoming.receive() as Frame.Text
                println(frame.readText())
                launch {
                    var totalSub = 0
                    while(true){
                        val writeMessage = writeMessageChannel.receive()
                        when(writeMessage.type){
                            STOMPMethod.SUBSCRIBE -> {
                                send("SUBSCRIBE\nid:sub-$totalSub\ndestination:/groups/${writeMessage.groupId}/messages\n\n")
                                send(EOF)
                                totalSub++
                            }
                            STOMPMethod.SEND -> {
                                println(writeMessage.message.length)
                                send("SEND\ndestination:/app/${writeMessage.groupId}/messages\ncontent-length:${writeMessage.message.length+1}\n\n${writeMessage.message}\n")
                                send(EOF)
                            }
                            else -> throw RuntimeException("Unsupported send of method ${writeMessage.type}")
                        }
                    }
                }
                while(!incoming.isClosedForReceive){
                    val text = (incoming.receive() as Frame.Text).readText().lines()
                    if(text.none()){
                        continue
                    }
                    text.first().let {
                        when(it) {
                            "MESSAGE" -> {
                                println("message")
                                var i = 1
                                val headers = mutableMapOf<String, String>()
                                while(text[i].isNotBlank()){
                                    headers[text[i].split(":")[0]] = text[i].split(":")[1]
                                    i++
                                }
                                try{
                                    val data = text.subList(i, text.size).joinToString("").let { temp ->
                                        Json.decodeFromString<MessageResponse>(temp.substring(0, temp.length - 1))
                                    }
                                    messageChannel.send(data)
                                }catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun closeSocket(){
        httpClient.close()
    }

    val groups: GroupsRoute = GroupsRoute()

    inner class GroupsRoute{
        suspend operator fun invoke(): List<Group> = httpClient.get(Groups()).body<List<GroupResponse>>().map { Group(it) }

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
                writeMessageChannel.send(WriteMessage(groupId = id, type = STOMPMethod.SUBSCRIBE))
            }

            suspend fun send(text: String){
                val data = Json.encodeToString<SendMessage>(SendMessage(text))
                writeMessageChannel.send(WriteMessage(data, id, STOMPMethod.SEND))
            }
        }
    }
}