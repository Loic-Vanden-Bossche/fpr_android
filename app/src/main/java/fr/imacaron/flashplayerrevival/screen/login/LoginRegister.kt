package fr.imacaron.flashplayerrevival.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.imacaron.flashplayerrevival.screen.Screen
import fr.imacaron.flashplayerrevival.state.viewmodel.LoginViewModel
import fr.imacaron.flashplayerrevival.utils.keyboardAsState

@Composable
fun LoginRegister(loginViewModel: LoginViewModel){

    val open by keyboardAsState()
    val (cardPlacement, pt) = if (open) {
        Arrangement.Top to 64.dp
    } else {
        Arrangement.Center to 0.dp
    }
    loginViewModel.init()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(top = pt).height(10.dp), verticalArrangement = cardPlacement) {
            Logo()
            NavHost(loginViewModel.loginNavigator, Screen.LoginScreen.route){
                composable(Screen.LoginScreen.route) { LoginCard(loginViewModel) }
                composable(Screen.RegisterScreen.route) { SignInCard(loginViewModel) }
            }
        }
    }
}