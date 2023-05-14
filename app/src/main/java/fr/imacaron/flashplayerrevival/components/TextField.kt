package fr.imacaron.flashplayerrevival.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun TextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	label: @Composable () -> Unit = {},
	trailingIcon: @Composable (() -> Unit)? = null,
	isError: Boolean = false,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default,
	singleLine: Boolean = false
){
	TextField(
		value,
		onValueChange,
		modifier,
		label = label,
		trailingIcon = trailingIcon,
		isError = isError,
		visualTransformation = visualTransformation,
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		colors = TextFieldDefaults.colors(
			focusedContainerColor = MaterialTheme.colorScheme.surface,
			unfocusedContainerColor = MaterialTheme.colorScheme.surface,
			focusedLabelColor = MaterialTheme.colorScheme.onSurface,
			unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
			focusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
			unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
			focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
			unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
			cursorColor = MaterialTheme.colorScheme.onSurfaceVariant,
			errorContainerColor = MaterialTheme.colorScheme.surface,
			errorTextColor = Color.Red,
			errorLabelColor = MaterialTheme.colorScheme.onSurface,
			errorCursorColor = MaterialTheme.colorScheme.onSurface,
			errorIndicatorColor = MaterialTheme.colorScheme.onSurface,
			errorTrailingIconColor = MaterialTheme.colorScheme.onSurface
		),
		singleLine = singleLine
	)
}