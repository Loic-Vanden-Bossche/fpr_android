package fr.imacaron.flashplayerrevival.screen

import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.screen.drawer.NavDrawerSheet
import fr.imacaron.flashplayerrevival.screen.home.HomeScreen
import fr.imacaron.flashplayerrevival.screen.message.MessageContainer
import fr.imacaron.flashplayerrevival.screen.search.SearchScreen
import fr.imacaron.flashplayerrevival.state.viewmodel.*
import java.util.*

@Composable
fun Main(drawerViewModel: DrawerViewModel, appViewModel: AppViewModel, homeViewModel: HomeViewModel, messageViewModel: MessageViewModel, searchViewModel: SearchViewModel){
    ModalNavigationDrawer({
        NavDrawerSheet(drawerViewModel, appViewModel)
    }, drawerState = drawerViewModel.drawerState){
        NavHost(drawerViewModel.mainNavigator, "home"){
            composable("home") {
                HomeScreen(drawerViewModel, homeViewModel)
            }
            composable(
                "message/{groupId}",
                listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("groupId")
                messageViewModel.currentGroup = UUID.fromString(id)
                MessageContainer(appViewModel, drawerViewModel, messageViewModel)
            }
            composable(Screen.SearchScreen.route){
                SearchScreen(searchViewModel)
            }
        }
    }
}