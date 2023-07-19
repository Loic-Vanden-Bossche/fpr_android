package fr.imacaron.flashplayerrevival.data.dto.out

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
	val token: String
)
