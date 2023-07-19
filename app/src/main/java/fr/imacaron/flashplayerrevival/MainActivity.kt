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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.data.repository.GroupRepository
import fr.imacaron.flashplayerrevival.data.repository.UserRepository
import fr.imacaron.flashplayerrevival.login.LoginRegister
import fr.imacaron.flashplayerrevival.login.Main
import fr.imacaron.flashplayerrevival.screen.Screen
import fr.imacaron.flashplayerrevival.screen.splash.Splash
import fr.imacaron.flashplayerrevival.state.viewmodel.AppViewModel
import fr.imacaron.flashplayerrevival.state.viewmodel.DrawerViewModel
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme

const val CHANNEL_ID = "a4c16ebd-8f12-4f43-8b87-6ad7d47a8f03"

class MainActivity : ComponentActivity() {

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")
        val tokenKey = stringPreferencesKey("token")
        val mailKey = stringPreferencesKey("email")
        val passwordKey = stringPreferencesKey("password")
    }

    val api: ApiService = ApiService(this)

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            val keyboardController = LocalSoftwareKeyboardController.current
            val drawerState = rememberDrawerState(DrawerValue.Closed) {
                keyboardController?.hide()
                true
            }
            val appNavigator = rememberNavController()
            val mainNavigator = rememberNavController()
            LaunchedEffect(Unit){
                intent.extras?.let {
                    it.getString("group")?.let { data ->
                        mainNavigator.navigate("message/$data")
                    }
                }
            }
            val appViewModel: AppViewModel = viewModel {
                AppViewModel(dataStore, UserRepository(), appNavigator) {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
            val drawerViewModel: DrawerViewModel = viewModel {
                DrawerViewModel(mainNavigator, GroupRepository(), UserRepository(), drawerState)
            }
            FlashPlayerRevivalTheme {
                NavHost(appNavigator, "splash") {
                    composable(Screen.SplashScreen.route){
                        Splash()
                    }
                    composable(Screen.AppScreen.route){
                        Main(drawerViewModel, appViewModel, this@MainActivity::messageNotification)
                    }
                    composable(Screen.LoginRegisterScreen.route){
                        LoginRegister(appNavigator, dataStore) {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

//    private suspend fun resume(){
//        token()?.let {
//            api.token = it
//            api.initSocket()
//        } ?: run {
//            startActivity(Intent(this, LoginActivity::class.java))
//        }
//    }

//    fun disconnect(){
//        runBlocking {
//            dataStore.edit { it.clear() }
//        }
//        finish()
//        startActivity(Intent(this, LoginActivity::class.java))
//    }

    private fun createNotificationChannel(){
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    fun messageNotification(message: ReceivedMessage){
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
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        with(notificationManager){
            notify(0, builder.build())
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
        }
    )
}