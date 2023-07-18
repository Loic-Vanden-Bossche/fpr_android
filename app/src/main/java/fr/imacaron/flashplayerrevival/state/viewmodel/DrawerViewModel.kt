package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.data.api.WebSocketService
import fr.imacaron.flashplayerrevival.data.repository.GroupRepository
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.screen.Screen
import java.util.*

class DrawerViewModel(
    val mainNavigator: NavHostController,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    val drawerState: DrawerState,
): ViewModel() {
    var selected: String by mutableStateOf("")

    var reload: Boolean by mutableStateOf(false)

    suspend fun navigateHome(){
        selected = ""
        mainNavigator.popBackStack(Screen.HomeScreen.route, false)
        drawerState.close()
    }

    suspend fun navigateSearch(){
        mainNavigator.navigate(Screen.SearchScreen.route)
        drawerState.close()
    }

    suspend fun navigateToMessage(id: UUID){
        mainNavigator.navigateUp()
        mainNavigator.navigate(Screen.MessageScreen(id).route)
        drawerState.close()
    }

    suspend fun getAllGroup(): List<GroupResponse> =
        groupRepository.getAll().onEach {
            WebSocketService.subscribeTo(UUID.fromString(it.id))
        }

    suspend fun getAllFriend(): List<UserResponse> =
        userRepository.getAllFriends()


}