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
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import fr.imacaron.flashplayerrevival.login.LoginActivity
import fr.imacaron.flashplayerrevival.login.LoginActivity.Companion.dataStore
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            FlashPlayerRevivalTheme {
                ModalNavigationDrawer({
                    NavDrawerSheet(drawerState)
                }, drawerState = drawerState){
                    Scaffold(topBar = { TopBar { scope.launch { drawerState.open() }}} ) {
                        Surface(
                            Modifier.fillMaxSize().padding(it),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Text("NIKKK")
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
fun NavDrawerSheet(drawerState: DrawerState){
    var selected by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = (LocalContext.current as MainActivity)
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary, drawerShape = RectangleShape) {
        RoundedTextField(search, { search = it }, label = { Text(stringResource(R.string.search_contact)) })
        Button({
            context.disconnect()
        }){
            Text("Se déconnecter")
        }
        LazyColumn {
            items(5){
                if(selected == it){
                    SelectedLine("Fred")
                }else{
                    Line("Fred"){
                        selected = it
                        scope.launch {
                            drawerState.close()
                        }
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
fun TopBar(nav: () -> Unit){
    TopAppBar(
        { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(nav){
                Icon(Icons.Default.Menu, "Nav")
            }
        }
    )
}