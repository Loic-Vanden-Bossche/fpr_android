package fr.imacaron.flashplayerrevival.messaging

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.TopBar
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserMessageResponse
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.utils.frenchDateFormater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun MessageContainer(groupId: UUID, self: UserResponse?, drawerState: DrawerState){
    val mainActivity = LocalContext.current as MainActivity
    val messages: MutableList<MessageResponse> = remember { mutableStateListOf() }
    var init by remember { mutableStateOf(false) }
    val newMessage by mainActivity.api.messageFlow.collectAsStateWithLifecycle(null)
    var input by remember { mutableStateOf("") }
    var group: ApiService.GroupsRoute.Group? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val messageState = rememberLazyListState()
    var longPress: MessageResponse? by remember { mutableStateOf(null) }
    LaunchedEffect(messageState.canScrollForward){
        if(!messageState.canScrollForward && messages.size >= 20){
            group?.messages(messages.size / 20, 20)?.forEach {
                messages.add(it)
            }
        }
    }
    LaunchedEffect(newMessage){
        newMessage?.let {
            if(it.group == groupId){
                messages.add(0, it.toMessageResponse())
            }else {
                mainActivity.messageNotification(it)
            }
        }
    }
    LaunchedEffect(mainActivity, groupId){
        if(!init) {
            mainActivity.api.groups(groupId).apply {
                messages.addAll(messages(0, 20).map { it })
                init = true
                group = this
            }
        }
    }
    Scaffold(
        bottomBar = { if(longPress != null) BottomBar(self, group, longPress!!, { removed -> messages.removeIf { it.id == removed } }) { longPress = null } },
        topBar = { TopBar(group?.name ?: "") { scope.launch { drawerState.open() }} }) {
        Surface(
            Modifier.fillMaxSize().padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(Modifier.fillMaxHeight()) {
                LazyColumn(Modifier.weight(1f), messageState, reverseLayout = true) {
                    items(messages){message ->
                        Message(message.message, message.createdAt, message.user, message.user.id == self?.id){
                            longPress = message
                        }
                    }
                }
                Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundedTextField(
                        input,
                        { input = it },
                        Modifier.weight(1f),
                        label = { Text(stringResource(R.string.write_message)) },
                        keyboardOptions = KeyboardOptions(KeyboardCapitalization.Sentences, true, KeyboardType.Text, ImeAction.Default),
                        maxLines = 3
                    )
                    ElevatedButton( {
                        scope.launch(Dispatchers.IO){
                            group?.send(input)
                            input = ""
                        }
                    }){
                        Text(stringResource(R.string.send))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Message(text: String, date: Date, user: UserMessageResponse, self: Boolean, onLongPress: () -> Unit){
    val haptic = LocalHapticFeedback.current
    val arrangement = if(self){
        Triple(Arrangement.End, TextAlign.End, Alignment.End)
    }else{
        Triple(Arrangement.Start, TextAlign.Start, Alignment.Start)
    }
    Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = arrangement.first) {
        Column(Modifier.padding(horizontal = 8.dp), horizontalAlignment = arrangement.third) {
            Text(user.nickname)
            ElevatedCard(Modifier.padding(top = 8.dp).combinedClickable(onClick = {  }, onLongClick = {haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongPress()})) {
                Column(Modifier.padding(8.dp).width(IntrinsicSize.Min)) {
                    Text(frenchDateFormater.format(date), Modifier.fillMaxWidth(), overflow = TextOverflow.Visible)
                    Text(text, Modifier.fillMaxWidth(), textAlign = arrangement.second)
                }
            }
        }
    }
}

@Composable
fun BottomBar(self: UserResponse?, group: ApiService.GroupsRoute.Group?, message: MessageResponse, onRemove: (UUID) -> Unit, close: () -> Unit){
    val scope = rememberCoroutineScope()
    BottomAppBar {
        IconButton(close){
            Icon(Icons.Default.Close, null)
        }
        Box(Modifier.weight(1f))
        if(message.user.id == self?.id){
            IconButton({}){
                Icon(Icons.Default.Edit, null)
            }
            IconButton({
                scope.launch { group?.deleteMessage(message.id) }
                onRemove(message.id)
                close()
            }){
                Icon(Icons.Default.Delete, null)
            }
        }
    }
}