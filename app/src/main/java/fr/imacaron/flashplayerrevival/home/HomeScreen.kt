package fr.imacaron.flashplayerrevival.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.TopBar
import fr.imacaron.flashplayerrevival.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(doReload: () -> Unit, drawerState: DrawerState){
    val mainActivity = LocalContext.current as MainActivity
    val friends: MutableList<ApiService.FriendsRoute.Friend> = remember { mutableStateListOf() }
    val pending: MutableList<ApiService.FriendsRoute.PendingRoute.Pending> = remember { mutableStateListOf() }
    var tab by remember { mutableStateOf(0) }
    var reload by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(mainActivity, reload){
        withContext(Dispatchers.IO){
            friends.clear()
            friends.addAll(mainActivity.api.friends())
            pending.clear()
            pending.addAll(mainActivity.api.friends.pending())
            doReload()
        }
    }
    Scaffold(
        topBar = { TopBar(stringResource(fr.imacaron.flashplayerrevival.R.string.app_name)) { scope.launch { drawerState.open() }} }) {
        Surface(
            Modifier.fillMaxSize().padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TabRow(tab){
                    Tab(tab == 0, { tab = 0 }, text = { Text("Friends") })
                    Tab(tab == 1, { tab = 1}, text = { Text("Pending") })
                }
                LazyColumn {
                    when(tab){
                        0 -> items(friends){
                            FriendLine(it) { reload = !reload }
                        }
                        1 -> items(pending){
                            PendingLine(it) { reload = !reload }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingLine(pending: ApiService.FriendsRoute.PendingRoute.Pending, reload: () -> Unit){
    val scope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(fr.imacaron.flashplayerrevival.R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(pending.nickname)
        Text(pending.email)
        IconButton({ scope.launch { pending.approve(); reload() } }){
            Icon(Icons.Default.Check, null)
        }
        IconButton({ scope.launch { pending.deny(); reload() } }){
            Icon(Icons.Default.Close, null)
        }
    }
}

@Composable
fun FriendLine(friend: ApiService.FriendsRoute.Friend, reload: () -> Unit){
    val scope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(fr.imacaron.flashplayerrevival.R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(friend.nickname)
        IconButton({ scope.launch { reload() } }){
            Icon(Icons.Default.Delete, null)
        }
    }
}