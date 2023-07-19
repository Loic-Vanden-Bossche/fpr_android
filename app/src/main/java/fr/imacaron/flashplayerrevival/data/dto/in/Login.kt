package fr.imacaron.flashplayerrevival.data.dto.`in`

import kotlinx.serialization.Serializable

@Serializable
data class Login(
	val email: String,
	val password: String
)
