package fr.imacaron.flashplayerrevival.api.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserMessageResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val email: String,
    val nickname: String
)
