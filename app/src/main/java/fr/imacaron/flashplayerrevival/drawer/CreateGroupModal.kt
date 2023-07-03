package fr.imacaron.flashplayerrevival.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.api.ApiService
import fr.imacaron.flashplayerrevival.api.dto.out.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupModal(api: ApiService, dismiss: () -> Unit, addGroup: (ApiService.GroupsRoute.Group) -> Unit, self: UserResponse?, friends: List<ApiService.FriendsRoute.Friend>){
    val check: MutableList<Boolean> = remember { mutableStateListOf() }
    val scope = rememberCoroutineScope()
    check.addAll(List(friends.size) { false })
    LaunchedEffect(friends){
        check.clear()
        check.addAll(List(friends.size) { false })
    }
    AlertDialog(dismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(Modifier.padding(24.dp)) {
                Text(stringResource(R.string.create_group), Modifier.padding(bottom = 16.dp), style = MaterialTheme.typography.headlineSmall)
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
                        Text(stringResource(R.string.cancel))
                    }
                    Button( {
                        scope.launch(Dispatchers.IO) {
                            addGroup(api.groups.create(friends.filterIndexed { index, _ -> check[index] }.map { it.original } + self!!))
                            dismiss()
                        }
                    } ){
                        Text(stringResource(R.string.create))
                    }
                }
            }
        }
    }
}