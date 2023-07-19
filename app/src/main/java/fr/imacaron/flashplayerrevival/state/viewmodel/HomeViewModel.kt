package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.flashplayerrevival.data.api.NoInternetException
import fr.imacaron.flashplayerrevival.data.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
	private val userRepository: UserRepository,
	private val appViewModel: AppViewModel
): ViewModel() {
	val friends: MutableList<UserResponse> = mutableStateListOf()

	val pending: MutableList<UserResponse> = mutableStateListOf()

	var refresh: Boolean by mutableStateOf(false)

	var reload: Boolean by mutableStateOf(false)

	var tab by mutableStateOf(0)

	fun getAllFriend() {
		viewModelScope.launch(Dispatchers.IO) {
			friends.clear()
			try {
				friends.addAll(userRepository.getAllFriends())
			}catch (e: NoInternetException){
				appViewModel.noConnection = true
			}
		}
	}

	fun getAllPending() {
		viewModelScope.launch(Dispatchers.IO) {
			pending.clear()
			try {
				pending.addAll(userRepository.getAllPending())
			}catch (e: NoInternetException){
				appViewModel.noConnection = true
			}
		}
	}

	fun approve(id: UUID) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				userRepository.approveFriend(id)
			}catch (e: NoInternetException){
				appViewModel.noConnection = true
			}
		}
		reload = !reload
	}

	fun deny(id: UUID) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				userRepository.denyFriend(id)
			}catch (e: NoInternetException){
				appViewModel.noConnection = true
			}
		}
		reload = !reload
	}

	fun delete(id: UUID) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				userRepository.deleteFriend(id)
			}catch (e: NoInternetException){
				appViewModel.noConnection = true
			}
		}
	}
}
