package fr.imacaron.flashplayerrevival.api.dto.`in`

import kotlinx.serialization.Serializable

@Serializable
data class Register(
	val email: String,
	val password: String,
	val username: String
)
