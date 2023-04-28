package fr.imacaron.flashplayerrevival.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PaleText(text: String){
	Text(text, color = MaterialTheme.colorScheme.secondary)
}