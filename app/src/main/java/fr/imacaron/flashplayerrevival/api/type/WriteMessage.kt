package fr.imacaron.flashplayerrevival.api.type

import java.util.*

data class WriteMessage(
    val message: String = "",
    val groupId: UUID,
    val type: STOMPMethod,
    val destination: String = ""
)
