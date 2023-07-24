package fr.imacaron.flashplayerrevival.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.data.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.data.repository.GroupRepository
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.screen.drawer.NavDrawerSheet
import fr.imacaron.flashplayerrevival.screen.home.HomeScreen
import fr.imacaron.flashplayerrevival.screen.message.MessageContainer
import fr.imacaron.flashplayerrevival.screen.search.ProfileScreen
import fr.imacaron.flashplayerrevival.screen.search.SearchScreen
import fr.imacaron.flashplayerrevival.state.viewmodel.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Main(appViewModel: AppViewModel, homeViewModel: HomeViewModel, messageNotification: (ReceivedMessage) -> Unit, intent: Intent){
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val drawerState = rememberDrawerState(DrawerValue.Closed) {
        keyboardController?.hide()
        true
    }
    val mainNavigator = rememberNavController()
    val drawerViewModel: DrawerViewModel = viewModel {
        DrawerViewModel(mainNavigator, GroupRepository(), UserRepository(), drawerState, appViewModel)
    }
    val searchViewModel: SearchViewModel = viewModel {
        SearchViewModel(UserRepository(), drawerViewModel.drawerState ,appViewModel)
    }
    val messageViewModel: MessageViewModel = viewModel {
        MessageViewModel(GroupRepository(),  drawerViewModel.mainNavigator, messageNotification, appViewModel)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(appViewModel.self, drawerViewModel)
    }
    LaunchedEffect(messageViewModel){
        messageViewModel.initNotification()
    }
    LaunchedEffect(intent){
        intent.extras?.let {
            it.getString("group")?.let { data ->
                drawerViewModel.navigateToMessage(UUID.fromString(data))
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> }
    LaunchedEffect(launcher) {
        if(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    ModalNavigationDrawer({
        NavDrawerSheet(drawerViewModel, appViewModel)
    }, drawerState = drawerViewModel.drawerState){
        NavHost(drawerViewModel.mainNavigator, "home"){
            composable(Screen.HomeScreen.route) {
                HomeScreen(drawerViewModel, homeViewModel, intent)
            }
            composable(
                Screen.MessageScreen.navRoute,
                listOf(navArgument(Screen.MessageScreen.paramName) { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString(Screen.MessageScreen.paramName)
                messageViewModel.currentGroup = UUID.fromString(id)
                MessageContainer(appViewModel, drawerViewModel, messageViewModel)
            }
            composable(Screen.SearchScreen.route){
                SearchScreen(searchViewModel)
            }
            composable(Screen.ProfileScreen.route) {
                ProfileScreen(profileViewModel)
            }
        }
    }
}