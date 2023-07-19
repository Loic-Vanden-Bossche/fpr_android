package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import fr.imacaron.flashplayerrevival.data.api.NoInternetException
import fr.imacaron.flashplayerrevival.data.api.WebSocketService
import fr.imacaron.flashplayerrevival.data.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.data.dto.out.MessageResponse
import fr.imacaron.flashplayerrevival.data.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.data.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class MessageViewModel(
	private val groupRepository: GroupRepository,
	mainNavigator: NavHostController,
	val messageNotification: (ReceivedMessage) -> Unit,
	private val appViewModel: AppViewModel
) : ViewModel() {

	private var notificationListener: Job? = null

	val messageFlow: StateFlow<ReceivedMessage?> = WebSocketService.messageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

	private var _currentGroup: UUID? by mutableStateOf(null)

	var messageEdit: MessageResponse? by mutableStateOf(null)

	var editMode: Boolean by mutableStateOf(false)

	var title: String by mutableStateOf("")

	var editingTitle: Boolean by mutableStateOf(false)

	var currentGroup: UUID?
		get() = _currentGroup
		set(value) {
			_currentGroup = value
			viewModelScope.launch(Dispatchers.IO){
				value?.let {
					try{
						group = groupRepository.get(value)
						messages.clear()
						messages.addAll(groupRepository.getAllMessage(it, 0, 20))
						title = group?.name ?: ""
					}catch (e: NoInternetException){
						appViewModel.noConnection = true
					}
				}
			}
		}

	val messages: MutableList<MessageResponse> = mutableStateListOf()

	var group: GroupResponse? by mutableStateOf(null)

	var input: String by mutableStateOf("")

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

	fun getMessages(page: Int, size: Int){
		currentGroup?.let {
			viewModelScope.launch(Dispatchers.IO){
				try {
					messages.addAll(groupRepository.getAllMessage(it, page, size))
				}catch (e: NoInternetException){
					appViewModel.noConnection = true
				}
			}
		}
	}

	fun sendMessage(){
		viewModelScope.launch(Dispatchers.IO){
			_currentGroup?.let {
				try {
					groupRepository.sendMessageToGroup(it, input)
					input = ""
				}catch (e: NoInternetException){
					appViewModel.noConnection = true
				}
			}
		}
	}

	fun editMessage(id: UUID){
		viewModelScope.launch(Dispatchers.IO){
			_currentGroup?.let {
				try {
					groupRepository.editMessageInGroup(it, input, id)
					input = ""
				}catch (e: NoInternetException){
					appViewModel.noConnection = true
				}
			}
		}
	}

	fun deleteMessage(id: UUID){
		viewModelScope.launch(Dispatchers.IO){
			_currentGroup?.let {
				try {
					groupRepository.deleteMessage(it, id)
				}catch (e: NoInternetException){
					appViewModel.noConnection = true
				}
			}
		}
	}

	fun editGroupName(){
		viewModelScope.launch {
			_currentGroup?.let {
				try {
					groupRepository.editGroupName(it, title)
				}catch (e: NoInternetException){
					appViewModel.noConnection = true
				}
			}
		}
	}

	override fun onCleared() {
		disconnectNotificationListener()
	}
}