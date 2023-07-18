package fr.imacaron.flashplayerrevival.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.components.pullrefresh.PullRefreshIndicator
import fr.imacaron.flashplayerrevival.components.pullrefresh.pullRefresh
import fr.imacaron.flashplayerrevival.components.pullrefresh.rememberPullRefreshState
import fr.imacaron.flashplayerrevival.state.viewmodel.AppViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.DrawerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun NavDrawerSheet(drawerViewModel: DrawerViewModel, appViewModel: AppViewModel){
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val groups: MutableList<GroupResponse> = remember { mutableStateListOf() }
    val friends: MutableList<UserResponse> = remember { mutableStateListOf() }
    var displayModal: Boolean by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var refreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullRefreshState(refreshing, {
        scope.launch(Dispatchers.IO) {
            refreshing = true
            groups.clear()
            groups.addAll(drawerViewModel.getAllGroup())
            refreshing = false
        }
    })
    LaunchedEffect(drawerViewModel.reload){
        groups.clear()
        groups.addAll(drawerViewModel.getAllGroup())
        friends.clear()
        friends.addAll(drawerViewModel.getAllFriend())
    }
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary, drawerShape = RectangleShape) {
        RoundedTextField(search, { search = it }, Modifier.padding(8.dp).fillMaxWidth(), label = { Text(stringResource(R.string.search_contact)) })
        Box(Modifier.weight(1f).pullRefresh(pullState)){
            LazyColumn(Modifier.fillMaxSize(), listState, flingBehavior = ScrollableDefaults.flingBehavior()) {
                if(!refreshing){
                    items(groups.filter { if(search.isBlank()) true else search in it.name.lowercase() }){
                        if(drawerViewModel.selected == it.id){
                            SelectedLine(it.name)
                        }else{
                            Line(it.name){
                                drawerViewModel.selected = it.id
                                scope.launch {
                                    drawerViewModel.drawerState.close()
                                    drawerViewModel.navigateToMessage(UUID.fromString(it.id))
                                }
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing,
                pullState,
                Modifier.align(alignment = Alignment.TopCenter)
            )
        }
        if(displayModal){
            CreateGroupModal({ displayModal = false }, { groups.add(0, it) }, appViewModel.self, friends)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconButton( { scope.launch { drawerViewModel.navigateHome() } } ){
                Icon(Icons.Default.Home, "Home")
            }
            IconButton( { displayModal = true } ){
                Icon(Icons.Default.GroupAdd, "Create group")
            }
            IconButton({ scope.launch { drawerViewModel.navigateSearch() } }){
                Icon(Icons.Default.PersonAdd, "Add friend")
            }
            IconButton({ appViewModel.disconnect() }){
                Icon(Icons.Default.Logout, "Log out")
            }
        }
    }
}

@Composable
fun Line(pseudo: String, onClick: () -> Unit){
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(pseudo, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SelectedLine(pseudo: String){
    Row(Modifier.shadow(10.dp, RectangleShape, spotColor = Color.Black).fillMaxWidth().background(MaterialTheme.colorScheme.surface), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(pseudo, color = MaterialTheme.colorScheme.onSurface)
    }
}