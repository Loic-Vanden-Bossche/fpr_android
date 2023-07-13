package fr.imacaron.flashplayerrevival.api.dto.`in`

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EditMessage(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val message: String
)