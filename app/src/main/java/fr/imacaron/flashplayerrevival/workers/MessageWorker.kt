package fr.imacaron.flashplayerrevival.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.data.api.WebSocketService
import fr.imacaron.flashplayerrevival.data.dto.out.MessageResponseType
import fr.imacaron.flashplayerrevival.data.dto.out.ReceivedMessage
import kotlinx.coroutines.flow.onEach

class MessageWorker(context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {

	private var ids: Int = 0

	override suspend fun doWork(): Result {
		(applicationContext as MainActivity).createNotificationChannel()
		WebSocketService.messageFlow.onEach { msg ->
			createForegroundInfo(msg)?.let {
				setForeground(it)
			}
		}
		return Result.success()
	}

	private fun createForegroundInfo(message: ReceivedMessage): ForegroundInfo? {
		if(message.type === MessageResponseType.NEW){
			val notification = (applicationContext as MainActivity).buildNotification(message)
			return ForegroundInfo(ids, notification).also { ids++ }
		}
		return null
	}
}