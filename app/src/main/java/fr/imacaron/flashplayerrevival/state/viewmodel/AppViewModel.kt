package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.data.api.ApiService
import fr.imacaron.flashplayerrevival.data.api.NoInternetException
import fr.imacaron.flashplayerrevival.data.api.WebSocketService
import fr.imacaron.flashplayerrevival.data.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    val dataStore: DataStore<Preferences>,
    private val userRepository: UserRepository,
    val appNavigator: NavHostController,
    private val makeToast: (String) -> Unit
): ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.data.map { it[MainActivity.tokenKey] }.first()?.let {
                ApiService.token = it
                WebSocketService.token = it
            }
            try {
                self = userRepository.self()
                launch {
                    WebSocketService.connectSocket()
                }
                withContext(Dispatchers.Main) {
                    appNavigator.navigate(Screen.AppScreen.route){
                        popUpTo(Screen.SplashScreen.route){
                            inclusive = true
                        }
                    }
                }
            }catch (e: Exception){
                if(e is NoInternetException){
                    withContext(Dispatchers.Main) {
                        appNavigator.navigate(Screen.AppScreen.route)
                        appNavigator.clearBackStack(Screen.SplashScreen.route)
                        noConnection = true
                    }
                }else {
                    dataStore.edit { it.remove(MainActivity.tokenKey) }
                    withContext(Dispatchers.Main){
                        appNavigator.navigate(Screen.LoginRegisterScreen.route)
                        appNavigator.clearBackStack(Screen.SplashScreen.route)
                    }
                }
            }
        }
    }

    var noConnection: Boolean by mutableStateOf(false)

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
        appNavigator.navigate(Screen.LoginRegisterScreen.route)
    }

    var self: UserResponse? by mutableStateOf(null)
}