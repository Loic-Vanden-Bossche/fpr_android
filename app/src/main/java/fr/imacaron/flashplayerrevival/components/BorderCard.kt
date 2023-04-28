package fr.imacaron.flashplayerrevival.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun BorderCard(
	modifier: Modifier = Modifier,
	shape: Shape = CardDefaults.elevatedShape,
	colors: CardColors = CardDefaults.elevatedCardColors(),
	elevation: CardElevation = CardDefaults.elevatedCardElevation(),
	content: @Composable ColumnScope.() -> Unit
) = Card(
	modifier.shadow(15.dp, shape = MaterialTheme.shapes.medium, spotColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)),
	shape,
	colors,
	elevation,
	BorderStroke(5.dp, MaterialTheme.colorScheme.primary),
	content
)