package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.flashplayerrevival.data.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
private val userRepository: UserRepository
): ViewModel() {
	val friends: MutableList<UserResponse> = mutableStateListOf()

	val pending: MutableList<UserResponse> = mutableStateListOf()

	var refresh: Boolean by mutableStateOf(false)

	var reload: Boolean by mutableStateOf(false)

	var tab by mutableStateOf(0)

	fun getAllFriend() {
		viewModelScope.launch(Dispatchers.IO) {
			friends.clear()
			friends.addAll(userRepository.getAllFriends())
		}
	}

	fun getAllPending() {
		viewModelScope.launch(Dispatchers.IO) {
			pending.clear()
			pending.addAll(userRepository.getAllPending())
		}
	}

	fun approve(id: UUID) {
		viewModelScope.launch(Dispatchers.IO) {
			userRepository.approveFriend(id)
		}
		reload = !reload
	}

	fun deny(id: UUID) {
		viewModelScope.launch(Dispatchers.IO) {
			userRepository.denyFriend(id)
		}
		reload = !reload
	}

	fun delete(id: UUID) {
		viewModelScope.launch(Dispatchers.IO) {
			userRepository.deleteFriend(id)
		}
	}
}
