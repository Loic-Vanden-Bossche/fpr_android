package fr.imacaron.flashplayerrevival.api.dto.`in`

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CreateGroup(
    val name: String,
    val users: List<@Serializable(with = UUIDSerializer::class) UUID>
)
