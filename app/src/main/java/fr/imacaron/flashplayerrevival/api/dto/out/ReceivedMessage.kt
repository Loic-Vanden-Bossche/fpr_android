package fr.imacaron.flashplayerrevival.api.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.DateSerializer
import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ReceivedMessage(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val user: UserMessageResponse,
    val message: String,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date,
    @Serializable(with = UUIDSerializer::class)
    val group: UUID,
    val type: MessageResponseType
){
    fun toMessageResponse(): MessageResponse = MessageResponse(id, user, message, createdAt, type)
}
