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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.login.LoginActivity
import fr.imacaron.flashplayerrevival.messaging.MessageContainer
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

const val CHANNEL_ID = "a4c16ebd-8f12-4f43-8b87-6ad7d47a8f03"

class MainActivity : ComponentActivity() {
    init {
        println("Construct class")
    }

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
        private val tokenKey = stringPreferencesKey("token")
    }

    lateinit var api: ApiService

    suspend fun token(): String? = dataStore.data.map { it[tokenKey] }.first()

    suspend fun token(token: String) = dataStore.edit { it[tokenKey] = token }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        intent.extras?.let {
            it.getString("token")?.let { token ->
                GlobalScope.launch {
                    token(token)
                    api = ApiService(token)
                    api.initSocket()
                }
            } ?: GlobalScope.launch { resume() }
        } ?: GlobalScope.launch { resume() }
        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val mainNav = rememberNavController()
            var title by remember { mutableStateOf("") }
            LaunchedEffect(Unit){
                intent.extras?.let {
                    it.getString("group")?.let { data ->
                        mainNav.navigate("message/$data")
                    }
                }
            }
            title = stringResource(R.string.app_name)
            FlashPlayerRevivalTheme {
                ModalNavigationDrawer({
                    NavDrawerSheet(drawerState, mainNav)
                }, drawerState = drawerState){
                    Scaffold(topBar = { TopBar(title) { scope.launch { drawerState.open() }}}) {
                        Surface(
                            Modifier.fillMaxSize().padding(it),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavHost(mainNav, "home"){
                                composable("home") {
                                    title = stringResource(R.string.app_name)
                                    Column {
                                        Text("NIKKK")
                                        Button({ messageNotification(UUID.fromString("4d6a7452-7048-4ca5-a364-e1e452fe50c3")) }){
                                            Text("Click me")
                                        }
                                    }
                                }
                                composable(
                                    "message/{groupId}",
                                    listOf(navArgument("groupId") { type = NavType.StringType })
                                ) { backStack ->
                                    val id = backStack.arguments?.getString("groupId")
                                    MessageContainer(UUID.fromString(id)) {newTitle ->
                                        title = newTitle
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun resume(){
        token()?.let {
            api = ApiService(it)
            api.initSocket()
        } ?: run {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
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

    fun messageNotification(groupId: UUID){
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtras(bundleOf("group" to groupId.toString()))
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Nouveau message")
            .setContentText("Texte du message")
            .setStyle(Notification.BigTextStyle().bigText("Texte du message mais vraiment long"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        with(notificationManager){
            notify(0, builder.build())
        }
    }
}

@Composable
fun NavDrawerSheet(drawerState: DrawerState, navigator: NavHostController){
    var selected by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = (LocalContext.current as MainActivity)
    val groups: MutableList<ApiService.GroupsRoute.Group> = remember { mutableStateListOf() }
    LaunchedEffect(context){
        context.api.groups().map {
            it.connnect()
            groups.add(it)
        }
    }
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary, drawerShape = RectangleShape) {
        RoundedTextField(search, { search = it }, label = { Text(stringResource(R.string.search_contact)) })
        Button({
            context.disconnect()
        }){
            Text("Se déconnecter")
        }
        LazyColumn {
            items(groups){
                if(selected == it.id.toString()){
                    SelectedLine(it.name)
                }else{
                    Line(it.name){
                        selected = it.id.toString()
                        scope.launch {
                            drawerState.close()
                        }
                        navigator.navigate("message/${it.id}")
                    }
                }
            }
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

@Composable
fun Line(pseudo: String, onClick: () -> Unit){
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.padding(all = 20.dp), shape = CircleShape, color = MaterialTheme.colorScheme.background) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(56.dp))
        }
        Text(pseudo, color = MaterialTheme.colorScheme.onBackground)
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