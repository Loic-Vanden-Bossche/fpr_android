package fr.imacaron.flashplayerrevival.data.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserGroupResponse(
    @Serializable(with = DateSerializer::class)
    val lastRead: Date,
    val user: UserResponse
)