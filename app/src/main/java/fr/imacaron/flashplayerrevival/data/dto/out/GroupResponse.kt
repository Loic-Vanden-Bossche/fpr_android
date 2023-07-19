package fr.imacaron.flashplayerrevival.data.dto.out

import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val type: GroupType,
    val members: List<UserGroupResponse>
)
