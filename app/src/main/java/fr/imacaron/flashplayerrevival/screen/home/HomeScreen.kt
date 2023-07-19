package fr.imacaron.flashplayerrevival.screen.home

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.TopBar
import fr.imacaron.flashplayerrevival.data.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.components.pullrefresh.PullRefreshIndicator
import fr.imacaron.flashplayerrevival.components.pullrefresh.pullRefresh
import fr.imacaron.flashplayerrevival.components.pullrefresh.rememberPullRefreshState
import fr.imacaron.flashplayerrevival.state.viewmodel.DrawerViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(drawerViewModel: DrawerViewModel, homeViewModel: HomeViewModel){
    val scope = rememberCoroutineScope()
    val pullState = rememberPullRefreshState(homeViewModel.refresh, {
        homeViewModel.refresh = true
        homeViewModel.getAllFriend()
        homeViewModel.getAllPending()
        homeViewModel.refresh = false
    })
    LaunchedEffect(homeViewModel.reload){
        withContext(Dispatchers.IO){
            homeViewModel.getAllFriend()
            homeViewModel.getAllPending()
            drawerViewModel.reload != drawerViewModel.reload
        }
    }
    Scaffold(
        topBar = { TopBar(stringResource(fr.imacaron.flashplayerrevival.R.string.app_name)) { scope.launch { drawerViewModel.drawerState.open() }} }) {
        Surface(
            Modifier.fillMaxSize().padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TabRow(homeViewModel.tab){
                    Tab(homeViewModel.tab == 0, { homeViewModel.tab = 0 }, text = { Text("Friends") })
                    Tab(homeViewModel.tab == 1, { homeViewModel.tab = 1}, text = { Text("Pending") })
                }
                Box(Modifier.weight(1f).pullRefresh(pullState)) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        if(!homeViewModel.refresh){
                            when(homeViewModel.tab){
                                0 -> items(homeViewModel.friends){
                                    FriendLine(it, homeViewModel)
                                }
                                1 -> items(homeViewModel.pending){
                                    PendingLine(it, homeViewModel)
                                }
                            }
                        }
                    }
                    PullRefreshIndicator(
                        homeViewModel.refresh,
                        pullState,
                        Modifier.align(alignment = Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun PendingLine(pending: UserResponse, homeViewModel: HomeViewModel){
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(fr.imacaron.flashplayerrevival.R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(pending.nickname)
        Text(pending.email)
        IconButton({ homeViewModel.approve(pending.id) }){
            Icon(Icons.Default.Check, null)
        }
        IconButton({ homeViewModel.deny(pending.id) }){
            Icon(Icons.Default.Close, null)
        }
    }
}

@Composable
fun FriendLine(friend: UserResponse, homeViewModel: HomeViewModel){
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(fr.imacaron.flashplayerrevival.R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(friend.nickname)
        IconButton({ homeViewModel.delete(friend.id) }){
            Icon(Icons.Default.Delete, null)
        }
    }
}