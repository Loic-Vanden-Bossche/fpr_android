package fr.imacaron.flashplayerrevival.screen

import java.util.*

sealed class Screen(val route: String) {
    object LoginRegisterScreen: Screen("loginRegister")
    object LoginScreen: Screen("login")
    object RegisterScreen: Screen("register")

    object AppScreen: Screen("app")

    object SplashScreen: Screen("splash")

    object HomeScreen: Screen("home")

    object SearchScreen: Screen("search")

    object ProfileScreen: Screen("profile")

    class MessageScreen(id: UUID): Screen("message/$id"){
        companion object {
            const val paramName: String = "id"
            const val navRoute: String = "message/{$paramName}"
        }
    }
}