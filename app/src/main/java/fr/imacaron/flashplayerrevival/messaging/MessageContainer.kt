package fr.imacaron.flashplayerrevival.messaging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserMessageResponse
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
    LaunchedEffect(mainActivity, groupId){
        mainActivity.api.groups(groupId).apply {
            messages.addAll(messages())
            setTitle(name)
        }
    }
    Column {
        Button({
            GlobalScope.launch(Dispatchers.IO){
                mainActivity.api.initSocket(groupId)
            }
        }){
            Text("Init socket")
        }
        Button({
            GlobalScope.launch(Dispatchers.IO){
                mainActivity.api.write(groupId, "{\"message\": \"Android dis coucou\"}")
            }
        }){
            Text("Send message")
        }
        LazyColumn {
            items(messages){message ->
                Message(message.message, message.createdAt, message.user)
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