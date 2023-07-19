package fr.imacaron.flashplayerrevival.login

import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.api.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.data.repository.GroupRepository
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.drawer.NavDrawerSheet
import fr.imacaron.flashplayerrevival.home.HomeScreen
import fr.imacaron.flashplayerrevival.messaging.MessageContainer
import fr.imacaron.flashplayerrevival.screen.Screen
import fr.imacaron.flashplayerrevival.search.SearchScreen
import fr.imacaron.flashplayerrevival.state.viewmodel.*
import java.util.*

@Composable
fun Main(drawerViewModel: DrawerViewModel, appViewModel: AppViewModel, messageNotification: (ReceivedMessage) -> Unit){
    val searchViewModel: SearchViewModel = viewModel {
        SearchViewModel(UserRepository(), drawerViewModel.drawerState)
    }
    val messageViewModel: MessageViewModel = viewModel {
        MessageViewModel(GroupRepository(),  drawerViewModel.mainNavigator, messageNotification)
    }
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(UserRepository())
    }
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