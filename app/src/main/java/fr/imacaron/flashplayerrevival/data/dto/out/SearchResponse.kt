package fr.imacaron.flashplayerrevival.data.dto.out

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SearchResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val email: String,
    val nickname: String,
    val status: String?
)
