package fr.imacaron.flashplayerrevival.api.dto.out

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
	val token: String
)
