package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import fr.imacaron.flashplayerrevival.api.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.data.api.WebSocketService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class MessageViewModel(
	private val mainNavigator: NavHostController,
	private val messageNotification: (ReceivedMessage) -> Unit
) : ViewModel() {

	private var notificationListener: Job? = null

	val messageFlow: StateFlow<ReceivedMessage?> = WebSocketService.messageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

	var currentGroup: UUID? by mutableStateOf(null)

	init {
		connectNotificationListener()
		mainNavigator.addOnDestinationChangedListener { _, destination, _ ->
			if(destination.route?.startsWith("message") == true){
				disconnectNotificationListener()
			}else {
				connectNotificationListener()
			}
		}
	}

	private fun connectNotificationListener(){
		notificationListener?.cancel()
		notificationListener = viewModelScope.launch {
			messageFlow.collectLatest {
				if(it?.group != currentGroup){
					it?.let(messageNotification)
				}
			}
		}
	}

	private fun disconnectNotificationListener(){
		notificationListener?.cancel()
		notificationListener = null
	}

	override fun onCleared() {
		disconnectNotificationListener()
	}
}