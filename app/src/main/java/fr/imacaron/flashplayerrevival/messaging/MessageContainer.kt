package fr.imacaron.flashplayerrevival.messaging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserMessageResponse
import fr.imacaron.flashplayerrevival.components.TextField
import fr.imacaron.flashplayerrevival.utils.frenchDateFormater
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun MessageContainer(groupId: UUID, setTitle: (String) -> Unit){
    val mainActivity = LocalContext.current as MainActivity
    val messages: MutableList<MessageResponse> = remember { mutableStateListOf() }
    var init by remember { mutableStateOf(false) }
    val newMessage by mainActivity.api.messageFlow.collectAsStateWithLifecycle(null)
    var input by remember { mutableStateOf("") }
    var group: ApiService.GroupsRoute.Group? by remember { mutableStateOf(null) }
    LaunchedEffect(newMessage){
        println(newMessage)
        newMessage?.let {
            messages.add(0, it)
        }
    }
    LaunchedEffect(mainActivity, groupId){
        if(!init) {
            mainActivity.api.groups(groupId).apply {
                messages.addAll(messages().map { it })
                setTitle(name)
                init = true
                group = this
            }
        }
    }
    Column(Modifier.fillMaxHeight()) {
        Row {
            TextField(
                input,
                { input = it }
            )
            Button( {
                GlobalScope.launch(Dispatchers.IO){
                    println("group")
                    group?.send(input)
                }
            }){
                Text("Send")
            }
        }
        LazyColumn(reverseLayout = true) {
            items(messages){message ->
                Message(message.message, message.createdAt, message.user)
//                Text(message)
            }
        }
    }
}

@Composable
fun Message(text: String, date: Date, user: UserMessageResponse){
    Row {
        Text(user.nickname)
        Column {
            Text(frenchDateFormater.format(date))
            Text(text)
        }
    }
}