package fr.imacaron.flashplayerrevival.data.repository

import fr.imacaron.flashplayerrevival.api.dto.`in`.CreateGroup
import fr.imacaron.flashplayerrevival.api.dto.`in`.DeleteMessage
import fr.imacaron.flashplayerrevival.api.dto.`in`.EditMessage
import fr.imacaron.flashplayerrevival.api.dto.`in`.SendMessage
import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.api.type.STOMPMethod
import fr.imacaron.flashplayerrevival.api.type.WriteMessage
import fr.imacaron.flashplayerrevival.data.api.ApiService
import fr.imacaron.flashplayerrevival.data.api.WebSocketService
import fr.imacaron.flashplayerrevival.data.api.resources.Groups
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class GroupRepository {

    suspend fun getAll(): List<GroupResponse> = ApiService.get(Groups())

    suspend fun get(id: UUID): GroupResponse = ApiService.get(Groups.Id(id = id))

    suspend fun create(users: List<UserResponse>): GroupResponse = ApiService.post(Groups(), CreateGroup(users.joinToString(", ") { it.nickname }, users.map { it.id }))

    suspend fun getAllMessage(group: UUID, page: Int, size: Int): List<MessageResponse> = ApiService.get(Groups.Id.Messages(Groups.Id(id = group), page, size))

    suspend fun sendMessageToGroup(group: UUID, text: String) {
        val data = Json.encodeToString<SendMessage>(SendMessage(text))
        WebSocketService.writeMessageChannel.send(WriteMessage(data, group, STOMPMethod.SEND))
    }

    suspend fun editMessageInGroup(group: UUID, message: String, id: UUID){
        val data = Json.encodeToString(EditMessage(id, message))
        WebSocketService.writeMessageChannel.send(WriteMessage(data, group, STOMPMethod.SEND, "/edit"))
    }

    suspend fun deleteMessage(group: UUID, id: UUID){
        val data = Json.encodeToString(DeleteMessage(id))
        WebSocketService.writeMessageChannel.send(WriteMessage(data, group, STOMPMethod.SEND, "/delete"))
    }
}