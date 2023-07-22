package fr.imacaron.flashplayerrevival

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import fr.imacaron.flashplayerrevival.data.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.screen.Main
import fr.imacaron.flashplayerrevival.screen.Screen
import fr.imacaron.flashplayerrevival.screen.login.LoginRegister
import fr.imacaron.flashplayerrevival.screen.splash.Splash
import fr.imacaron.flashplayerrevival.state.viewmodel.AppViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.HomeViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.LoginViewModel
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import fr.imacaron.flashplayerrevival.workers.MessageWorker

const val CHANNEL_ID = "a4c16ebd-8f12-4f43-8b87-6ad7d47a8f03"

class MainActivity : ComponentActivity() {

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")
        val tokenKey = stringPreferencesKey("token")
        val mailKey = stringPreferencesKey("email")
        val passwordKey = stringPreferencesKey("password")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifeCycleListener(this::startListening, this::stopListening))
        setContent {
            val appNavigator = rememberNavController()
            val loginNav = rememberNavController()
            val appViewModel: AppViewModel = viewModel {
                AppViewModel(dataStore, UserRepository(), appNavigator) {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
            val loginViewModel = viewModel {
                LoginViewModel(loginNavigator = loginNav, appNavigator = appViewModel.appNavigator, dataStore = appViewModel.dataStore, makeToast = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() }, appViewModel = appViewModel)
            }
            val homeViewModel: HomeViewModel = viewModel {
                HomeViewModel(UserRepository(), appViewModel)
            }
            FlashPlayerRevivalTheme {
                Box{
                    NavHost(appNavigator, Screen.SplashScreen.route) {
                        composable(Screen.SplashScreen.route){
                            Splash()
                        }
                        composable(Screen.AppScreen.route){
                            Main(appViewModel, homeViewModel, this@MainActivity::messageNotification, intent)
                        }
                        composable(Screen.LoginRegisterScreen.route){
                            LoginRegister(loginViewModel)
                        }
                    }
                    if(appViewModel.noConnection){
                        Surface(color = MaterialTheme.colorScheme.surface) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SignalCellularConnectedNoInternet0Bar, "No internet")
                                Text("No internet", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }

    fun createNotificationChannel(){
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    fun messageNotification(message: ReceivedMessage){
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        with(notificationManager){
            notify(0, buildNotification(message))
        }
    }

    fun buildNotification(message: ReceivedMessage): Notification{
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtras(bundleOf("group" to message.group.toString()))
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(getString(R.string.new_message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if(message.message.length > 15){
            builder
                .setContentText(message.message.take(15) + "…")
                .setStyle(Notification.BigTextStyle().bigText(message.message))
        }else{
            builder
                .setContentText(message.message)
        }
        return builder.build()
    }

    private var work: OneTimeWorkRequest? = null

    private fun startListening() {
        if(work == null){
            work = OneTimeWorkRequestBuilder<MessageWorker>().build()
            work?.let {
                WorkManager.getInstance(this).enqueue(it)
            }
        }
    }

    private fun stopListening() {
        work?.let {
            WorkManager.getInstance(this).cancelWorkById(it.id)
        }
    }
}

class AppLifeCycleListener(private val startListening: () -> Unit, private val stopListening: () -> Unit): DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        stopListening()
    }

    override fun onStop(owner: LifecycleOwner) {
        startListening()
    }
}