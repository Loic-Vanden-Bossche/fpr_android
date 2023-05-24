package fr.imacaron.flashplayerrevival.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.edit
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.components.BorderCard
import fr.imacaron.flashplayerrevival.components.PaleText
import fr.imacaron.flashplayerrevival.components.PasswordField
import fr.imacaron.flashplayerrevival.components.TextField
import fr.imacaron.flashplayerrevival.login.LoginActivity.Companion.dataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun LoginCard(goToSignIn: () -> Unit) {
	val context = (LocalContext.current as LoginActivity)
	var mail by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var error by remember { mutableStateOf(false) }
	var loading by remember { mutableStateOf(false) }
	LaunchedEffect(context){
		context.dataStore.data.map { it[LoginActivity.mailKey] }.first()?.let { mail = it }
		context.dataStore.data.map { it[LoginActivity.passwordKey] }.first()?.let { password = it }
		if(mail != "" && password != ""){
			loading = true
			withContext(Dispatchers.IO){
				if(context.connect(mail, password)){
					mail = ""
					password = ""
				}else {
					error = true
				}
				loading = false
			}
		}
	}
	BorderCard(Modifier.padding(8.dp)) {
		Column(
			Modifier.fillMaxWidth().padding(32.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Column(Modifier.fillMaxWidth()) {
				Text(stringResource(R.string.welcome), style = MaterialTheme.typography.displayMedium)
				Text(stringResource(R.string.cred), style = MaterialTheme.typography.titleLarge)
			}
			TextField(
				mail,
				{ mail = it },
				Modifier.fillMaxWidth(),
				{ Text(stringResource(R.string.mail)) },
				isError = error,
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
				singleLine = true
			)
			PasswordField(
				password,
				{ password = it },
				Modifier.fillMaxWidth(),
				{ Text(stringResource(R.string.password)) },
				isError = error,
				keyboardActions = KeyboardActions(onDone = {
					loading = true
					GlobalScope.launch(Dispatchers.IO) {
						if(context.connect(mail, password)){
							mail = ""
							password = ""
						}else {
							error = true
						}
						loading = false
					}
				})
			)
			Button(
				{
					loading = true
					GlobalScope.launch(Dispatchers.IO) {
						if(context.connect(mail, password)){
							mail = ""
							password = ""
						}else {
							error = true
						}
						loading = false
					}
				},
				Modifier.fillMaxWidth().shadow(
					10.dp,
					shape = MaterialTheme.shapes.extraLarge,
					spotColor = MaterialTheme.colorScheme.primary
				),
				enabled = !loading
			) {
				Text(stringResource(R.string.signin))
			}
			Row(verticalAlignment = Alignment.CenterVertically) {
				PaleText(stringResource(R.string.no_account))
				TextButton(goToSignIn) {
					Text(stringResource(R.string.signup))
				}
			}
		}
	}
}