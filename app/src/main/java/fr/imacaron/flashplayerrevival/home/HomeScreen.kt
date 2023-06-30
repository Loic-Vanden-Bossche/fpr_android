package fr.imacaron.flashplayerrevival.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(){
    val mainActivity = LocalContext.current as MainActivity
    val friends: MutableList<ApiService.FriendsRoute.Friend> = remember { mutableStateListOf() }
    LaunchedEffect(mainActivity){
        withContext(Dispatchers.IO){
        friends.addAll(mainActivity.api.friends())
        }
    }
    LazyColumn {
        items(friends){
            Text(it.nickname)
        }
    }
}