package fr.imacaron.flashplayerrevival.screen.message

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.data.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.data.dto.out.UserMessageResponse
import fr.imacaron.flashplayerrevival.data.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.state.viewmodel.AppViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.DrawerViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.MessageViewModel
import fr.imacaron.flashplayerrevival.utils.frenchDateFormater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun MessageContainer(appViewModel: AppViewModel, drawerViewModel: DrawerViewModel, messageViewModel: MessageViewModel) {
	val scope = rememberCoroutineScope()
	val messageState = rememberLazyListState()
	LaunchedEffect(messageState.canScrollForward) {
		if (!messageState.canScrollForward && messageViewModel.messages.size >= 20) {
			messageViewModel.getMessages(messageViewModel.messages.size / 20, 20)
		}
	}
	Scaffold(
		bottomBar = {
			if (messageViewModel.messageEdit != null) {
				BottomBar(
					appViewModel.self,
					messageViewModel,
					messageViewModel.messageEdit!!,
					{ messageViewModel.messageEdit = null },
					{
						messageViewModel.editMode = true
						messageViewModel.input = it.message
					}
				)
			}
		},
		topBar = { TopBar(messageViewModel) { scope.launch { drawerViewModel.drawerState.open() } } }) {
		Surface(
			Modifier.fillMaxSize().padding(it),
			color = MaterialTheme.colorScheme.background
		) {
			Column(Modifier.fillMaxHeight()) {
				LazyColumn(Modifier.weight(1f), messageState, reverseLayout = true) {
					items(messageViewModel.messages) { message ->
						Message(
							message.message,
							message.createdAt,
							message.user,
							message.user.id == appViewModel.self?.id
						) {
							messageViewModel.messageEdit = message
						}
					}
				}
				Row(
					Modifier.padding(horizontal = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					RoundedTextField(
						messageViewModel.input,
						{ messageViewModel.input = it },
						Modifier.weight(1f),
						enabled = messageViewModel.messageEdit == null || messageViewModel.editMode,
						label = { Text(stringResource(R.string.write_message)) },
						keyboardOptions = KeyboardOptions(
							KeyboardCapitalization.Sentences,
							true,
							KeyboardType.Text,
							ImeAction.Default
						),
						maxLines = 3
					)
					ElevatedButton(
						{
							scope.launch(Dispatchers.IO) {
								if (messageViewModel.editMode) {
									messageViewModel.editMessage(messageViewModel.messageEdit!!.id)
									messageViewModel.messageEdit = null
								} else {
									messageViewModel.sendMessage()
								}
							}
						},
						enabled = messageViewModel.messageEdit == null || messageViewModel.editMode
					) {
						Text(stringResource(R.string.send))
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Message(text: String, date: Date, user: UserMessageResponse, self: Boolean, onLongPress: () -> Unit) {
	val haptic = LocalHapticFeedback.current
	val arrangement = if (self) {
		Triple(Arrangement.End, TextAlign.End, Alignment.End)
	} else {
		Triple(Arrangement.Start, TextAlign.Start, Alignment.Start)
	}
	Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = arrangement.first) {
		Column(Modifier.padding(horizontal = 8.dp), horizontalAlignment = arrangement.third) {
			Column(horizontalAlignment = arrangement.third) {
				Text(user.nickname)
				Text(frenchDateFormater.format(date), Modifier, overflow = TextOverflow.Visible)
			}
			ElevatedCard(
				Modifier.padding(top = 8.dp).combinedClickable(onClick = { }, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); if (self) onLongPress() })
			) {
				Column(Modifier.padding(8.dp)) {
					Text(text, Modifier, textAlign = arrangement.second)
				}
			}
		}
	}
}

@Composable
fun BottomBar(
	self: UserResponse?,
	messageViewModel: MessageViewModel,
	message: MessageResponse,
	close: () -> Unit,
	onEdit: (MessageResponse) -> Unit
) {
	val scope = rememberCoroutineScope()
	BottomAppBar {
		IconButton(close) {
			Icon(Icons.Default.Close, null)
		}
		Box(Modifier.weight(1f))
		if (message.user.id == self?.id) {
			IconButton({
				onEdit(message)
			}) {
				Icon(Icons.Default.Edit, null)
			}
			IconButton({

				scope.launch { messageViewModel.deleteMessage(message.id) }
				close()
			}) {
				Icon(Icons.Default.Delete, null)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(messageViewModel: MessageViewModel, nav: () -> Unit) {
	TopAppBar(
		{
			if (messageViewModel.editingTitle) {
				RoundedTextField(messageViewModel.title, { messageViewModel.title = it })
			} else {
				Text(messageViewModel.title)
			}
		},
		navigationIcon = {
			IconButton(nav) {
				Icon(Icons.Default.Menu, "Nav")
			}
		},
		actions = {
			if (messageViewModel.editingTitle) {
				IconButton({
					messageViewModel.editingTitle = false
					messageViewModel.editGroupName()
				}) {
					Icon(Icons.Default.Save, "Save name")
				}
			} else {
				IconButton({
					messageViewModel.editingTitle = true
				}) {
					Icon(Icons.Default.Edit, "Edit name")
				}
			}
		}
	)
}