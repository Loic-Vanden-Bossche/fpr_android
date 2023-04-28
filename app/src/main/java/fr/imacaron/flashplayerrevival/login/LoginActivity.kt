package fr.imacaron.flashplayerrevival.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.components.BorderCard
import fr.imacaron.flashplayerrevival.components.PaleText
import fr.imacaron.flashplayerrevival.components.TextField
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import fr.imacaron.flashplayerrevival.utils.keyboardAsState
import java.util.*

class LoginActivity: ComponentActivity() {
	@SuppressLint("SourceLockedOrientationActivity")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent {
			val open by keyboardAsState()
			val (cardPlacement, pt) = if(open){
				Arrangement.Top to 64.dp
			}else {
				Arrangement.Center to 0.dp
			}
			FlashPlayerRevivalTheme {
				Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					Column(Modifier.padding(top = pt).height(10.dp), verticalArrangement = cardPlacement) {
						Logo()
						LoginCard()
					}
				}
			}
		}
	}
}

@Composable
fun Logo(){
	Row(Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
		Image(painterResource(R.drawable.banner), "Banner")
	}
}

@Composable
fun LoginCard(){
	val context = LocalContext.current
	var mail by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	BorderCard(Modifier.padding(8.dp)){
		Column(Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
			Column(Modifier.fillMaxWidth()) {
				Text(stringResource(R.string.welcome), style = MaterialTheme.typography.displayMedium)
				Text(stringResource(R.string.cred), style = MaterialTheme.typography.titleLarge)
			}
			TextField(
				mail,
				{ mail = it },
				Modifier.fillMaxWidth(),
				{ Text(stringResource(R.string.mail)) },
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
				singleLine = true
			)
			TextField(password,
				{ password = it },
				Modifier.fillMaxWidth(),
				{ Text(stringResource(R.string.password)) },
				visualTransformation = PasswordVisualTransformation(),
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
				keyboardActions = KeyboardActions(onDone = { connect(mail, password, context) }),
				singleLine = true
			)
			Button({ connect(mail, password, context) }, Modifier.fillMaxWidth().shadow(10.dp, shape = MaterialTheme.shapes.extraLarge, spotColor = MaterialTheme.colorScheme.primary)) {
				Text(stringResource(R.string.signin))
			}
			Row(verticalAlignment = Alignment.CenterVertically) {
				PaleText(stringResource(R.string.no_account))
				TextButton({}){
					Text(stringResource(R.string.signup))
				}
			}
		}
	}
}

fun connect(mail: String, password: String, context: Context){
	println("mail = $mail, password = $password")
	context.startActivity(Intent(context, MainActivity::class.java))

}