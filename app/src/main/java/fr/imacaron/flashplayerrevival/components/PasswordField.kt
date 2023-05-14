package fr.imacaron.flashplayerrevival.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	label: @Composable () -> Unit = {},
	isError: Boolean = false,
	keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
	keyboardActions: KeyboardActions = KeyboardActions.Default
){
	var show by remember { mutableStateOf(false) }
	TextField(
		value,
		onValueChange,
		modifier,
		label,
		{
			IconButton({
				show = !show
			}){ Icon(if(show) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Show Password") }
		},
		isError = isError,
		visualTransformation = if(show) VisualTransformation.None else PasswordVisualTransformation(),
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		singleLine = true
	)
}