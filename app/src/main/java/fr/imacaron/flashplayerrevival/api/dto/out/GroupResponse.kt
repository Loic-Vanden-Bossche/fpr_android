package fr.imacaron.flashplayerrevival.api.dto.out

import fr.imacaron.flashplayerrevival.domain.type.GroupType
import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val type: GroupType,
    val members: List<UserResponse>
)
