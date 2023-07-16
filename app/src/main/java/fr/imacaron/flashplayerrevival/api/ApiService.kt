package fr.imacaron.flashplayerrevival.api

import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.api.dto.`in`.*
import fr.imacaron.flashplayerrevival.api.dto.out.*
import fr.imacaron.flashplayerrevival.api.resources.Friends
import fr.imacaron.flashplayerrevival.api.resources.Groups
import fr.imacaron.flashplayerrevival.api.resources.Profile
import fr.imacaron.flashplayerrevival.api.resources.Users
import fr.imacaron.flashplayerrevival.api.type.STOMPMethod
import fr.imacaron.flashplayerrevival.api.type.WriteMessage
import fr.imacaron.flashplayerrevival.domain.type.GroupType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
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
import java.net.ConnectException
import java.util.*
import kotlin.collections.set

class ApiService(activity: MainActivity) {

    companion object {
        const val HOST = "api.flash-player-revival.net"
//        const val HOST = "192.168.1.63"
    }

    var token: String = ""

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
//                protocol = URLProtocol.HTTP
                protocol = URLProtocol.HTTPS
                host = HOST
//                port = 8080
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
                    HttpStatusCode.Unauthorized -> {
                        activity.disconnect()
                    }
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

    private suspend fun DefaultClientWebSocketSession.receive(){
        try {
            for(message in incoming){
                val text = (message as? Frame.Text ?: continue).readText().lines()
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
                                    UUID.fromString(headers["destination"]!!.split("/")[2]),
                                    data.type
                                )
                                messageChannel.send(msg)
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }catch (e: Exception){
            println("Error in receive : ${e.localizedMessage}")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun DefaultClientWebSocketSession.send(){
        var totalSub = 0
        while(true){
            while(writeMessageChannel.isEmpty){
                if(outgoing.isClosedForSend){
                    return
                }
            }
            val writeMessage = writeMessageChannel.receive()
            try {
                when(writeMessage.type){
                    STOMPMethod.SUBSCRIBE -> {
                        send("SUBSCRIBE\nid:sub-$totalSub\ndestination:/groups/${writeMessage.groupId}/messages\n\n")
                        send(EOF)
                        totalSub++
                    }
                    STOMPMethod.SEND -> {
                        send("SEND\ndestination:/app/${writeMessage.groupId}/messages${writeMessage.destination}\ncontent-length:${writeMessage.message.length+1}\n\n${writeMessage.message}\n")
                        send(EOF)
                    }
                    else -> throw RuntimeException("Unsupported send of method ${writeMessage.type}")
                }
            }catch (e: Exception){
                println("Error in send : ${e.localizedMessage}")
                return
            }
        }
    }

    suspend fun initSocket(){
        withContext(Dispatchers.IO){
            var nTry = 0
            while (true) {
                try {
                    httpClient.webSocket(method = HttpMethod.Get, host = HOST, path = "/socket") {
                        send("CONNECT\nAuthorization:$token\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n".encodeToByteArray())
                        send(byteArrayOf(0))
                        incoming.receive() as Frame.Text

                        val messageOutputRoutine = launch { receive() }
                        val userInput = launch { send() }

                        this@ApiService.groups().forEach { it.connect() }

                        userInput.join()
                        messageOutputRoutine.cancelAndJoin()
                        nTry = 0
                    }
                }catch (e: ConnectException){
                    println(e.localizedMessage)
                    nTry++
                }
                delay(1000L + 5000L * nTry)
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
            val members: List<UserResponse> get() = original.members.map { it.user }

            suspend fun messages(page: Int, size: Int): List<MessageResponse> = httpClient.get(Groups.Id.Messages(Groups.Id(id = UUID.fromString(original.id)), page, size)).body()

            suspend fun connect(){
                writeMessageChannel.send(WriteMessage(groupId = id, type = STOMPMethod.SUBSCRIBE))
            }

            suspend fun send(text: String){
                val data = Json.encodeToString<SendMessage>(SendMessage(text))
                writeMessageChannel.send(WriteMessage(data, id, STOMPMethod.SEND))
            }

            suspend fun editMessage(message: String, id: UUID){
                val data = Json.encodeToString(EditMessage(id, message))
                writeMessageChannel.send(WriteMessage(data, this@Group.id, STOMPMethod.SEND, "/edit"))
            }

            suspend fun deleteMessage(id: UUID){
                val data = Json.encodeToString(DeleteMessage(id))
                writeMessageChannel.send(WriteMessage(data, this@Group.id, STOMPMethod.SEND, "/delete"))
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

            suspend fun delete(){
                httpClient.delete(Friends.Id(id = this.id))
            }
        }
    }

    val users: UsersRoute = UsersRoute()

    inner class UsersRoute {
        suspend fun search(search: String): List<Search> = httpClient.get(Users.Search(search = search)).body<List<SearchResponse>>().map { Search(it) }

        inner class Search(val original: SearchResponse) {
            val id: UUID get() = original.id
            val email: String get() = original.email
            val nickname: String get() = original.nickname
            val status: String? get() = original.status

            suspend fun addFriend(){
                httpClient.post(Friends()){
                    contentType(ContentType.Application.Json)
                    setBody(AddFriend(id))
                }
            }
        }
    }
}