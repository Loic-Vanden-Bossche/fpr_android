package fr.imacaron.flashplayerrevival.data.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

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
    val picture: String? = null
)
