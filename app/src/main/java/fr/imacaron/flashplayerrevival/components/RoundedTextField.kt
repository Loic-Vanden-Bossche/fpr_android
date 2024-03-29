package fr.imacaron.flashplayerrevival.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RoundedTextField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	label: @Composable () -> Unit = {},
	trailingIcon: @Composable (() -> Unit)? = null,
	isError: Boolean = false,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default,
	singleLine: Boolean = false,
	maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
){
	TextField(
		value,
		onValueChange,
		modifier.padding(0.dp),
		enabled,
		label,
		trailingIcon,
		isError,
		visualTransformation,
		keyboardOptions,
		keyboardActions,
		singleLine,
		maxLines,
		RoundedCornerShape(8.dp)
	)
}