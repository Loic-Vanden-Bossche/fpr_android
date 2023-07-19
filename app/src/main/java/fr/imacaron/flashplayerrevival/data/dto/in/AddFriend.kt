package fr.imacaron.flashplayerrevival.data.dto.`in`

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AddFriend(
    @Serializable(with = UUIDSerializer::class)
    val friend: UUID
)
