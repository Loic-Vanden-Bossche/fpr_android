package fr.imacaron.flashplayerrevival.drawer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import fr.imacaron.flashplayerrevival.Line
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.SelectedLine
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import fr.imacaron.flashplayerrevival.components.RoundedTextField
import kotlinx.coroutines.launch

@Composable
fun NavDrawerSheet(drawerState: DrawerState, navigator: NavHostController, self: UserResponse?){
    var selected by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = (LocalContext.current as MainActivity)
    val groups: MutableList<ApiService.GroupsRoute.Group> = remember { mutableStateListOf() }
    var displayModal: Boolean by remember { mutableStateOf(false) }
    LaunchedEffect(context){
        context.api.groups().map {
            it.connect()
            groups.add(it)
        }
    }
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary, drawerShape = RectangleShape) {
        RoundedTextField(search, { search = it }, label = { Text(stringResource(R.string.search_contact)) })
        LazyColumn(Modifier.weight(1f)) {
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
        if(displayModal){
            CreateGroupModal(context.api,  { displayModal = false }, { groups.add(0, it) }, self)
        }
        Row {
            IconButton( { displayModal = true } ){
                Icon(Icons.Default.GroupAdd, "Create group")
            }
            IconButton({}){
                Icon(Icons.Default.PersonAdd, null)
            }
            IconButton({ context.disconnect() }){
                Icon(Icons.Default.Logout, "Log out")
            }
        }
    }
}