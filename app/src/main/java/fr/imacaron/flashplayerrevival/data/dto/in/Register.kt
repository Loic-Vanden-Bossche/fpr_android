package fr.imacaron.flashplayerrevival.data.dto.`in`

import kotlinx.serialization.Serializable

@Serializable
data class Register(
	val email: String,
	val password: String,
	val username: String
)
