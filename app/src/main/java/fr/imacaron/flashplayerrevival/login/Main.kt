package fr.imacaron.flashplayerrevival.login

import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.drawer.NavDrawerSheet
import fr.imacaron.flashplayerrevival.screen.Screen
import fr.imacaron.flashplayerrevival.search.SearchScreen
import fr.imacaron.flashplayerrevival.state.viewmodel.AppViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.DrawerViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.SearchViewModel

@Composable
fun Main(drawerViewModel: DrawerViewModel, appViewModel: AppViewModel){
    val searchViewModel: SearchViewModel = viewModel {
        SearchViewModel(UserRepository(), drawerViewModel.drawerState)
    }
    var title by remember { mutableStateOf("") }
    title = stringResource(R.string.app_name)
    ModalNavigationDrawer({
        NavDrawerSheet(drawerViewModel, appViewModel)
    }, drawerState = drawerViewModel.drawerState){
        NavHost(drawerViewModel.mainNavigator, "home"){
            composable("home") {
//                                    val newMessage by api.messageFlow.collectAsStateWithLifecycle(null)
//                                    LaunchedEffect(newMessage){
//                                        newMessage?.let { msg ->
//                                            messageNotification(msg)
//                                        }
//                                    }
//                                    HomeScreen({ reload = !reload }, drawerState)
            }
            composable(
                "message/{groupId}",
                listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("groupId")
//                                    MessageContainer(UUID.fromString(id), self, drawerState)
            }
            composable(Screen.SearchScreen.route){
                SearchScreen(searchViewModel)
            }
        }
    }
}