package fr.imacaron.flashplayerrevival.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.components.BorderCard
import fr.imacaron.flashplayerrevival.components.PaleText
import fr.imacaron.flashplayerrevival.components.PasswordField
import fr.imacaron.flashplayerrevival.components.TextField
import fr.imacaron.flashplayerrevival.state.viewmodel.LoginViewModel

@Composable
fun LoginCard(viewModel: LoginViewModel) {
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
				viewModel.email,
				{ viewModel.email = it },
				Modifier.fillMaxWidth(),
				label = { Text(stringResource(R.string.mail)) },
				isError = viewModel.error,
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
				singleLine = true
			)
			PasswordField(
				viewModel.password,
				{ viewModel.password = it },
				Modifier.fillMaxWidth(),
				{ Text(stringResource(R.string.password)) },
				isError = viewModel.error,
				keyboardActions = KeyboardActions(onDone = {
					viewModel.login()
				})
			)
			Button(
				{
					viewModel.login()
				},
				Modifier.fillMaxWidth().shadow(
					10.dp,
					shape = MaterialTheme.shapes.extraLarge,
					spotColor = MaterialTheme.colorScheme.primary
				),
				enabled = !viewModel.loading
			) {
				Text(stringResource(R.string.signin))
			}
			Row(verticalAlignment = Alignment.CenterVertically) {
				PaleText(stringResource(R.string.no_account))
				TextButton({ viewModel.toSignIn() }) {
					Text(stringResource(R.string.signup))
				}
			}
		}
	}
}