package fr.imacaron.flashplayerrevival.api

import fr.imacaron.flashplayerrevival.api.dto.`in`.CreateGroup
import fr.imacaron.flashplayerrevival.api.dto.`in`.SendMessage
import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.api.resources.Friends
import fr.imacaron.flashplayerrevival.api.resources.Groups
import fr.imacaron.flashplayerrevival.api.resources.Profile
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

    private val messageChannel: Channel<ReceivedMessage> = Channel()

    val messageFlow: Flow<ReceivedMessage> = flow {
        while(true){
            val data = messageChannel.receive()
            emit(data)
        }
    }

    private val writeMessageChannel: Channel<WriteMessage> = Channel()

    private val EOF: ByteArray = byteArrayOf(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun initSocket(){
        withContext(Dispatchers.IO){
            httpClient.webSocket(method = HttpMethod.Get, host = "192.168.1.63", port = 8080, path = "/socket"){
                send("CONNECT\nAuthorization:$token\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n".encodeToByteArray())
                send(byteArrayOf(0))
                incoming.receive() as Frame.Text
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
                                    val msg = ReceivedMessage(
                                        data.id,
                                        data.user,
                                        data.message,
                                        data.createdAt,
                                        UUID.fromString(headers["destination"]!!.split("/")[2])
                                    )
                                    messageChannel.send(msg)
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

    val self: SelfRoute = SelfRoute()

    val groups: GroupsRoute = GroupsRoute()

    val friends: FriendsRoute = FriendsRoute()

    inner class SelfRoute {
        suspend operator fun invoke(): UserResponse = httpClient.get(Profile()).body()
    }

    inner class GroupsRoute{
        suspend operator fun invoke(): List<Group> = httpClient.get(Groups()).body<List<GroupResponse>>().map { Group(it) }

        suspend operator fun invoke(id: UUID): Group = Group(httpClient.get(Groups.Id(id = id)).body())

        suspend fun create(data: List<UserResponse>): Group =
            httpClient.post(Groups()) {
                val name = data.joinToString(", ") { it.nickname }
                contentType(ContentType.Application.Json)
                setBody(CreateGroup(name, data.map { it.id }))
            }.let { Group(it.body()).apply { connect() } }

        inner class Group(
            private val original: GroupResponse
        ) {
            val id: UUID get() = UUID.fromString(original.id)
            val name: String get() = original.name
            val type: GroupType get() = original.type
            val members: List<UserResponse> get() = original.members

            suspend fun messages(page: Int, size: Int): List<MessageResponse> = httpClient.get(Groups.Id.Messages(Groups.Id(id = UUID.fromString(original.id)), page, size)).body()

            suspend fun connect(){
                writeMessageChannel.send(WriteMessage(groupId = id, type = STOMPMethod.SUBSCRIBE))
            }

            suspend fun send(text: String){
                val data = Json.encodeToString<SendMessage>(SendMessage(text))
                writeMessageChannel.send(WriteMessage(data, id, STOMPMethod.SEND))
            }

            suspend fun deleteMessage(id: UUID){
//                httpClient.delete(Groups.Id.Messages.Id(Groups.Id.Messages(Groups.Id(id = this.id)), id))
                httpClient.delete("/api/groups/${this.id}/messages/$id")
            }
        }
    }
    
    inner class FriendsRoute{

        suspend operator fun invoke(): List<FriendsRoute.Friend> = httpClient.get(Friends()).body<List<UserResponse>>().map { Friend(it) }

        suspend operator fun invoke(id: UUID): FriendsRoute.Friend = Friend(httpClient.get(Friends.Id(id = id)).body())

        val pending: PendingRoute = PendingRoute()

        inner class PendingRoute {
            suspend operator fun invoke(): List<FriendsRoute.PendingRoute.Pending> = httpClient.get(Friends.Pending()).body<List<UserResponse>>().map { Pending(it) }

            inner class Pending(
                val original: UserResponse
            ){
                val id: UUID get() = original.id
                val email: String get() = original.email
                val role: String get() = original.role
                val nickname: String get() = original.nickname
                val coins: Int get() = original.coins
                val updatedAt: String get() = original.updatedAt
                val createdAt: String get() = original.createdAt

                suspend fun approve(){
                    httpClient.patch(Friends.Approve(id = id))
                }

                suspend fun deny(){
                    httpClient.patch(Friends.Deny(id = id))
                }
            }
        }

        inner class Friend(
            val original: UserResponse
        ){
            val id: UUID get() = original.id
            val email: String get() = original.email
            val role: String get() = original.role
            val nickname: String get() = original.nickname
            val coins: Int get() = original.coins
            val updatedAt: String get() = original.updatedAt
            val createdAt: String get() = original.createdAt
        }
    }
}