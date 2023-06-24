package fr.imacaron.flashplayerrevival

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.GroupResponse
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.login.LoginActivity
import fr.imacaron.flashplayerrevival.login.LoginActivity.Companion.dataStore
import fr.imacaron.flashplayerrevival.messaging.MessageContainer
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import kotlinx.coroutines.*
import java.util.*

class MainActivity : ComponentActivity() {

    lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = ApiService(intent.extras!!.getString("token")!!)
        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val mainNav = rememberNavController()
            var title by remember { mutableStateOf("") }
            title = stringResource(R.string.app_name)
            FlashPlayerRevivalTheme {
                ModalNavigationDrawer({
                    NavDrawerSheet(drawerState, mainNav)
                }, drawerState = drawerState){
                    Scaffold(topBar = { TopBar(title) { scope.launch { drawerState.open() }}} ) {
                        Surface(
                            Modifier.fillMaxSize().padding(it),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavHost(mainNav, "home"){
                                composable("home") {
                                    Text("NIKKK")
                                    title = stringResource(R.string.app_name)
                                }
                                composable(
                                    "message/{groupId}",
                                    listOf(navArgument("groupId") { type = NavType.StringType })
                                ) { backStack ->
                                    val groupId = backStack.arguments?.getString("groupId")
                                    MessageContainer(UUID.fromString(groupId)) {newTitle ->
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

    fun disconnect(){
        runBlocking {
            dataStore.edit { it.clear() }
        }
        finish()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}

@Composable
fun NavDrawerSheet(drawerState: DrawerState, navigator: NavHostController){
    var selected by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = (LocalContext.current as MainActivity)
    val groups: MutableList<GroupResponse> = remember { mutableStateListOf() }
    LaunchedEffect(context){
        groups.addAll(context.api.groups())
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
                if(selected == it.id){
                    SelectedLine(it.name)
                }else{
                    Line(it.name){
                        selected = it.id
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