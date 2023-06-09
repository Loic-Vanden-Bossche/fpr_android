package fr.imacaron.flashplayerrevival.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.TopBar
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(api: ApiService, drawerState: DrawerState){
    var job: Job? by remember { mutableStateOf(null) }
    var search by remember { mutableStateOf("") }
    val users: MutableList<ApiService.UsersRoute.Search> = remember { mutableStateListOf() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(search){
        job?.let { if(it.isActive) it.cancel() }
        job = launch {
            delay(500)
            val res = if(search.isNotBlank()){
                api.users.search(search.trim())
            }else{
                mutableListOf()
            }
            users.clear()
            users.addAll(res)
        }
    }
    Scaffold(
        topBar = { TopBar(stringResource(R.string.app_name)) { scope.launch { drawerState.open() }} }) { pv ->
        Surface(
            Modifier.fillMaxSize().padding(pv),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                RoundedTextField(search, { search = it }, Modifier.padding(8.dp).fillMaxWidth(), { Text(stringResource(R.string.search_user)) })
                LazyColumn {
                    items(users) {
                        UserLine(it)
                    }
                }
            }
        }
    }
}

@Composable
fun UserLine(user: ApiService.UsersRoute.Search){
    val scope = rememberCoroutineScope()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(user.nickname)
        when(user.status){
            "APPROVED" -> Icon(Icons.Default.Done, null)
            "REJECTED" -> Icon(Icons.Default.Close, null)
            "PENDING" -> Icon(Icons.Default.HourglassEmpty, null)
            else -> IconButton({ scope.launch { user.addFriend() } }){
                Icon(Icons.Default.PersonAdd, null)
            }
        }
    }
}