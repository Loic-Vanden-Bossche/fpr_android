package fr.imacaron.flashplayerrevival.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class Login(
	val email: String,
	val password: String
)
