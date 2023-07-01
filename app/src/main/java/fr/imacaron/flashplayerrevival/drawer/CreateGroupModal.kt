package fr.imacaron.flashplayerrevival.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupModal(api: ApiService, dismiss: () -> Unit, addGroup: (ApiService.GroupsRoute.Group) -> Unit, self: UserResponse?){
    val friends: MutableList<ApiService.FriendsRoute.Friend> = remember { mutableStateListOf() }
    val check: MutableList<Boolean> = remember { mutableStateListOf() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(api){
        val data = api.friends()
        friends.addAll(data)
        check.addAll(List(data.size) { false })
    }
    AlertDialog(dismiss, modifier = Modifier.height((180 + friends.size * 50).dp)) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(Modifier.padding(24.dp)) {
                Text("Créer un groupe", Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.headlineSmall)
                LazyColumn(Modifier.height((friends.size * 50).dp)) {
                    itemsIndexed(friends, { i, _ -> i }, { _, v -> v }){ i, v ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(v.nickname)
                            Checkbox(check[i], { check[i] = !check[i] })
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    Button( { dismiss() } ){
                        Text("Annuler")
                    }
                    Button( {
                        scope.launch(Dispatchers.IO) {
                            addGroup(api.groups.create(friends.filterIndexed { index, _ -> check[index] }.map { it.original } + self!!))
                            dismiss()
                        }
                    } ){
                        Text("Créer")
                    }
                }
            }
        }
    }
}