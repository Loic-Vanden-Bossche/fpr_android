package fr.imacaron.flashplayerrevival.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.imacaron.flashplayerrevival.R

@Composable
fun Splash(){
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.fpr_logo_background), "Splash Logo", Modifier.size(196.dp))
        Image(painterResource(R.drawable.fpr_logo_foreground), "Splash Logo", Modifier.size(196.dp))
    }
}