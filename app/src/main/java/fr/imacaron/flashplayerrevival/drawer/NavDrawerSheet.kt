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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.SelectedLine
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.components.pullrefresh.PullRefreshIndicator
import fr.imacaron.flashplayerrevival.components.pullrefresh.pullRefresh
import fr.imacaron.flashplayerrevival.components.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavDrawerSheet(drawerState: DrawerState, navigator: NavHostController, self: UserResponse?, reload: Boolean){
    var selected by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = (LocalContext.current as MainActivity)
    val groups: MutableList<ApiService.GroupsRoute.Group> = remember { mutableStateListOf() }
    val friends: MutableList<ApiService.FriendsRoute.Friend> = remember { mutableStateListOf() }
    var displayModal: Boolean by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var refreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullRefreshState(refreshing, {
        scope.launch(Dispatchers.IO) {
            refreshing = true
            groups.clear()
            context.api.groups().forEach {
                groups.add(it)
            }
            refreshing = false
        }
    })
    LaunchedEffect(context, reload){
        groups.clear()
        context.api.groups().forEach {
            groups.add(it)
        }
        friends.clear()
        friends.addAll(context.api.friends())
    }
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary, drawerShape = RectangleShape) {
        RoundedTextField(search, { search = it }, Modifier.padding(8.dp).fillMaxWidth(), label = { Text(stringResource(R.string.search_contact)) })
        Box(Modifier.weight(1f).pullRefresh(pullState)){
            LazyColumn(Modifier.fillMaxSize(), listState, flingBehavior = ScrollableDefaults.flingBehavior()) {
                if(!refreshing){
                    items(groups.filter { if(search.isBlank()) true else search in it.name.lowercase() }){
                        if(selected == it.id.toString()){
                            SelectedLine(it.name)
                        }else{
                            Line(it.name){
                                selected = it.id.toString()
                                scope.launch {
                                    drawerState.close()
                                }
                                navigator.navigateUp()
                                navigator.navigate("message/${it.id}")
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
            CreateGroupModal(context.api,  { displayModal = false }, { groups.add(0, it) }, self, friends)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconButton( { selected = ""; navigator.popBackStack("home", false); scope.launch { drawerState.close() } } ){
                Icon(Icons.Default.Home, "Home")
            }
            IconButton( { displayModal = true } ){
                Icon(Icons.Default.GroupAdd, "Create group")
            }
            IconButton({ navigator.navigate("search"); scope.launch { drawerState.close() } }){
                Icon(Icons.Default.PersonAdd, null)
            }
            IconButton({ context.disconnect() }){
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