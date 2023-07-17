package fr.imacaron.flashplayerrevival.data.repository

import fr.imacaron.flashplayerrevival.api.dto.`in`.Login
import fr.imacaron.flashplayerrevival.api.dto.`in`.Register
import fr.imacaron.flashplayerrevival.api.dto.out.LoginResponse
import fr.imacaron.flashplayerrevival.data.api.ApiService
import fr.imacaron.flashplayerrevival.data.api.resources.Auth

class AuthRepository {
    suspend fun login(email: String, password: String): LoginResponse = ApiService.post(Auth.Login(), Login(email, password))

    suspend fun register(email: String, password: String, pseudo: String): LoginResponse = ApiService.post(Auth.Register(), Register(email, password, pseudo))
}