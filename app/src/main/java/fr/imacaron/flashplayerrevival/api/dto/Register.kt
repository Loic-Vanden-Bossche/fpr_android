package fr.imacaron.flashplayerrevival.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class Register(
	val email: String,
	val password: String
)
