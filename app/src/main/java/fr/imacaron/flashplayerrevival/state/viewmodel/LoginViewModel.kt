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
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.data.api.NoInternetException
import fr.imacaron.flashplayerrevival.data.error.InvalidField
import fr.imacaron.flashplayerrevival.data.error.LoginError
import fr.imacaron.flashplayerrevival.data.repository.AuthRepository
import fr.imacaron.flashplayerrevival.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    val loginNavigator: NavHostController,
    private val appNavigator: NavHostController,
    private val dataStore: DataStore<Preferences>,
    private val makeToast: (resId: Int) -> Unit,
    private val appViewModel: AppViewModel
): ViewModel() {

    var email: String by mutableStateOf("")
    var password: String by mutableStateOf("")
    var pseudo: String by mutableStateOf("")
    var error: Boolean by mutableStateOf(false)
    var loading: Boolean by mutableStateOf(false)

    fun init() {
        viewModelScope.launch(Dispatchers.IO){
            val email = dataStore.data.map { it[MainActivity.mailKey] }.first()?.also { email = it }
            val password = dataStore.data.map { it[MainActivity.passwordKey] }.first()?.also { password = it }
            if(email != null && password != null){
                login()
            }
        }
    }

    fun login() {
        loading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = authRepository.login(email, password)
                dataStore.edit { settings ->
                    settings[MainActivity.mailKey] = email
                    settings[MainActivity.passwordKey] = password
                    settings[MainActivity.tokenKey] = token.token
                }
                withContext(Dispatchers.Main){
                    appNavigator.navigate(Screen.AppScreen.route)
                }
            } catch (e: NoInternetException){
                appViewModel.noConnection = true
            }catch (_: LoginError){
                withContext(Dispatchers.Main){
                    makeToast(R.string.login_error)
                }
                error = true
            }catch (_: InvalidField){
                withContext(Dispatchers.Main){
                    makeToast(R.string.invalid_field)
                }
                error = true
            }finally {
                loading = false
            }
        }
    }

    fun register() {
        loading = true
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val token = authRepository.register(email, password, pseudo)
                dataStore.edit { settings ->
                    settings[MainActivity.mailKey] = email
                    settings[MainActivity.passwordKey] = password
                    settings[MainActivity.tokenKey] = token.token
                }
                appNavigator.navigate(Screen.AppScreen.route)
            }catch (e: NoInternetException){
                appViewModel.noConnection = true
            }catch (e: Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    makeToast(R.string.new_message)
                }
                //TODO Error management
                error = true
            }
            finally {
                loading = false
            }
        }
    }

    fun toSignIn(){
        loginNavigator.navigate(Screen.RegisterScreen.route)
    }

    fun toLogIn(){
        loginNavigator.navigate(Screen.LoginScreen.route)
    }

}