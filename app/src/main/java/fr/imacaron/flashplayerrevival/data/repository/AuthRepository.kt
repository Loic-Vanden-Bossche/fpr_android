package fr.imacaron.flashplayerrevival.data.repository

import fr.imacaron.flashplayerrevival.data.dto.`in`.Login
import fr.imacaron.flashplayerrevival.data.dto.`in`.Register
import fr.imacaron.flashplayerrevival.data.dto.out.LoginResponse
import fr.imacaron.flashplayerrevival.data.api.ApiService
import fr.imacaron.flashplayerrevival.data.api.resources.Auth

class AuthRepository {
    suspend fun login(email: String, password: String): LoginResponse = ApiService.post<Auth.Login, LoginResponse, Login>(Auth.Login(), Login(email, password)).apply {
        ApiService.token = token
    }

    suspend fun register(email: String, password: String, pseudo: String): LoginResponse = ApiService.post<Auth.Register, LoginResponse, Register>(Auth.Register(), Register(email, password, pseudo)).apply {
        ApiService.token = token
    }
}