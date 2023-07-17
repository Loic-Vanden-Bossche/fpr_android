package fr.imacaron.flashplayerrevival.data.api

import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.api.type.STOMPMethod
import fr.imacaron.flashplayerrevival.api.type.WriteMessage
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.net.ConnectException
import java.util.*

object WebSocketService {

    val EOF: ByteArray = byteArrayOf(0)

    private val messageChannel: Channel<ReceivedMessage> = Channel()

    val writeMessageChannel: Channel<WriteMessage> = Channel()

    var token: String = ""

    var connecting: Boolean = false

    private var groups: MutableList<UUID> = mutableListOf()

    val messageFlow: Flow<ReceivedMessage> = flow {
        while(true){
            val data = messageChannel.receive()
            emit(data)
        }
    }

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

    suspend fun connectSocket(){
        withContext(Dispatchers.IO){
            var nTry = 0
            while (true) {
                try {
                    HttpClient().webSocket(method = HttpMethod.Get, host = ApiService.HOST, path = "/socket") {
                        connecting = false
                        send("CONNECT\nAuthorization:$token\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n".encodeToByteArray())
                        send(EOF)
                        incoming.receive() as Frame.Text

                        val receiving = launch { receive() }
                        val userInput = launch { send() }

                        groups.forEach { writeMessageChannel.send(WriteMessage(groupId = it, type = STOMPMethod.SUBSCRIBE)) }

                        userInput.join()
                        receiving.cancelAndJoin()
                        connecting = true
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

    suspend fun subscribeTo(id: UUID){
        writeMessageChannel.send(WriteMessage(groupId = id, type = STOMPMethod.SUBSCRIBE))
        groups.add(id)
    }
}