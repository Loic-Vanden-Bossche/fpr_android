package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.flashplayerrevival.data.dto.out.SearchResponse
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SearchViewModel(
    private val userRepository: UserRepository,
    val drawerState: DrawerState
): ViewModel() {
    var search by mutableStateOf("")

    val users: MutableList<SearchResponse> = mutableStateListOf()

    fun search() {
        viewModelScope.launch(Dispatchers.IO) {
            users.clear()
            if (search != "") {
                users.addAll(userRepository.search(search))
            }
        }
    }

    fun addFriend(id: UUID){
        viewModelScope.launch(Dispatchers.IO){
            userRepository.addFriend(id)
        }
    }
}