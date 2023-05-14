package fr.imacaron.flashplayerrevival.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
	val token: String
)
