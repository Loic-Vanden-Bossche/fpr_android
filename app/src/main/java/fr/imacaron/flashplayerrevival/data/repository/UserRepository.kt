package fr.imacaron.flashplayerrevival.data.repository

import fr.imacaron.flashplayerrevival.api.dto.`in`.AddFriend
import fr.imacaron.flashplayerrevival.api.dto.out.SearchResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.data.api.ApiService
import fr.imacaron.flashplayerrevival.data.api.resources.Friends
import fr.imacaron.flashplayerrevival.data.api.resources.Profile
import fr.imacaron.flashplayerrevival.data.api.resources.Users
import java.util.*

class UserRepository {

    suspend fun self(): UserResponse = ApiService.get<Profile, UserResponse>(Profile())

    suspend fun search(search: String): List<SearchResponse> = ApiService.get(Users.Search(search = search))

    suspend fun addFriend(id: UUID): Unit = ApiService.post(Friends(), AddFriend(id))

    suspend fun getAllFriends(): List<UserResponse> = ApiService.get(Friends())

    suspend fun getAllPending(): List<UserResponse> = ApiService.get(Friends.Pending())

    suspend fun approveFriend(id: UUID): Unit = ApiService.patch(Friends.Approve(id = id), Unit)

    suspend fun denyFriend(id: UUID): Unit = ApiService.patch(Friends.Deny(id = id), Unit)

    suspend fun deleteFriend(id: UUID): Unit = ApiService.delete(Friends.Id(id = id))
}