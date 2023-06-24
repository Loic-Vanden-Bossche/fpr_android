package fr.imacaron.flashplayerrevival.api.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.DateSerializer
import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class MessageResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val user: UserMessageResponse,
    val message: String,
    @Serializable(with = DateSerializer::class)
    val createdAt: Date
)
