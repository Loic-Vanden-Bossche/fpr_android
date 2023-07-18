package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fr.imacaron.flashplayerrevival.api.dto.out.SearchResponse
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import java.util.*

class SearchViewModel(
    private val userRepository: UserRepository,
    val drawerState: DrawerState
): ViewModel() {
    var search by mutableStateOf("")

    val users: MutableList<SearchResponse> = mutableStateListOf()

    suspend fun search() {
        users.clear()
        if(search != ""){
            users.addAll(userRepository.search(search))
        }
    }

    suspend fun addFriend(id: UUID){
        userRepository.addFriend(id)
    }
}