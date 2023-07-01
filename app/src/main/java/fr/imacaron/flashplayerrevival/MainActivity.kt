package fr.imacaron.flashplayerrevival

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.ReceivedMessage
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.drawer.NavDrawerSheet
import fr.imacaron.flashplayerrevival.home.HomeScreen
import fr.imacaron.flashplayerrevival.login.LoginActivity
import fr.imacaron.flashplayerrevival.messaging.MessageContainer
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

const val CHANNEL_ID = "a4c16ebd-8f12-4f43-8b87-6ad7d47a8f03"

class MainActivity : ComponentActivity() {

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
        private val tokenKey = stringPreferencesKey("token")
    }

    lateinit var api: ApiService

    suspend fun token(): String? = dataStore.data.map { it[tokenKey] }.first()

    suspend fun token(token: String) = dataStore.edit { it[tokenKey] = token }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.let {
            it.getString("token")?.let { token ->
                GlobalScope.launch {
                    token(token)
                    api = ApiService(token)
                    api.initSocket()
                }
            } ?: GlobalScope.launch { resume() }
        } ?: GlobalScope.launch { resume() }
        createNotificationChannel()
        setContent {
            val keyboardController = LocalSoftwareKeyboardController.current
            val drawerState = rememberDrawerState(DrawerValue.Closed) {
                if (it == DrawerValue.Open) {
                    keyboardController?.hide()
                }
                true
            }
            val mainNav = rememberNavController()
            var title by remember { mutableStateOf("") }
            var self: UserResponse? by remember { mutableStateOf(null) }
            var reload by remember { mutableStateOf(false) }
            LaunchedEffect(Unit){
                intent.extras?.let {
                    it.getString("group")?.let { data ->
                        mainNav.navigate("message/$data")
                    }
                }
                self = api.self()
            }
            title = stringResource(R.string.app_name)
            FlashPlayerRevivalTheme {
                ModalNavigationDrawer({
                    NavDrawerSheet(drawerState, mainNav, self, reload)
                }, drawerState = drawerState){
                    NavHost(mainNav, "home"){
                        composable("home") {
                            val newMessage by api.messageFlow.collectAsStateWithLifecycle(null)
                            LaunchedEffect(newMessage){
                                newMessage?.let { msg ->
                                    messageNotification(msg)
                                }
                            }
                            HomeScreen({ reload = !reload }, drawerState)
                        }
                        composable(
                            "message/{groupId}",
                            listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { backStack ->
                            val id = backStack.arguments?.getString("groupId")
                            MessageContainer(UUID.fromString(id), self, drawerState)
                        }
                    }
                }
            }
        }
    }

    private suspend fun resume(){
        token()?.let {
            api = ApiService(it)
            api.initSocket()
        } ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun disconnect(){
        runBlocking {
            dataStore.edit { it.clear() }
        }
        finish()
        startActivity(Intent(this, LoginActivity::class.java))
    }

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

@Composable
fun SelectedLine(pseudo: String){
    Row(Modifier.shadow(10.dp, RectangleShape, spotColor = Color.Black).fillMaxWidth().background(MaterialTheme.colorScheme.surface), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(pseudo, color = MaterialTheme.colorScheme.onSurface)
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