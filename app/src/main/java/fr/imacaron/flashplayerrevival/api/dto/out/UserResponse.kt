package fr.imacaron.flashplayerrevival.api.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val email: String,
    val role: String,
    val nickname: String,
    val coins: Int,
    val updatedAt: String,
    val createdAt: String,
)
