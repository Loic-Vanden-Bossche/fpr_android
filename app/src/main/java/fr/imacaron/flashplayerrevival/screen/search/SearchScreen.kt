package fr.imacaron.flashplayerrevival.screen.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.data.dto.out.SearchResponse
import fr.imacaron.flashplayerrevival.state.viewmodel.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(searchViewModel: SearchViewModel){
    var job: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(searchViewModel.search){
        job?.let { if(it.isActive) it.cancel() }
        job = launch {
            delay(500)
            searchViewModel.search()
        }
    }
    Scaffold(
        topBar = { TopBar(stringResource(R.string.app_name)) { scope.launch { searchViewModel.drawerState.open() }} }) { pv ->
        Surface(
            Modifier.fillMaxSize().padding(pv),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                RoundedTextField(searchViewModel.search, { searchViewModel.search = it }, Modifier.padding(8.dp).fillMaxWidth(), label = { Text(stringResource(R.string.search_user)) })
                LazyColumn {
                    items(searchViewModel.users) {
                        UserLine(it, searchViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun UserLine(user: SearchResponse, searchViewModel: SearchViewModel){
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(user.nickname)
        when(user.status){
            "APPROVED" -> Icon(Icons.Default.Done, null)
            "REJECTED" -> Icon(Icons.Default.Close, null)
            "PENDING" -> Icon(Icons.Default.HourglassEmpty, null)
            else -> IconButton({ searchViewModel.addFriend(user.id); searchViewModel.search() }){
                Icon(Icons.Default.PersonAdd, null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, nav: () -> Unit){
    TopAppBar(
        { Text(title) },
        navigationIcon = {
            IconButton(nav){
                Icon(Icons.Default.Menu, "Nav")
            }
        },
        actions = {
            IconButton({}){
                Icon(Icons.Default.Person, "Profile")
            }
        }
    )
}