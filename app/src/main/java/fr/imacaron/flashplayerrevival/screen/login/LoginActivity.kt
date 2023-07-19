package fr.imacaron.flashplayerrevival.screen.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import fr.imacaron.flashplayerrevival.MainActivity
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.data.api.resources.Auth
import fr.imacaron.flashplayerrevival.data.dto.`in`.Login
import fr.imacaron.flashplayerrevival.data.dto.`in`.Register
import fr.imacaron.flashplayerrevival.data.dto.out.LoginResponse
import fr.imacaron.flashplayerrevival.data.error.ConflictingUser
import fr.imacaron.flashplayerrevival.data.error.InvalidField
import fr.imacaron.flashplayerrevival.data.error.LoginError
import fr.imacaron.flashplayerrevival.ui.theme.FlashPlayerRevivalTheme
import fr.imacaron.flashplayerrevival.utils.keyboardAsState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class LoginActivity : ComponentActivity() {
	private val httpClient = HttpClient {
		install(Resources)
		install(ContentNegotiation){
			json()
		}
		defaultRequest {
			url {
//				protocol = URLProtocol.HTTP
				protocol = URLProtocol.HTTPS
				host = "api.flash-player-revival.net"
//				host = "192.168.1.63"
//				port = 8080
				path("api/")
			}
		}
		expectSuccess = true
		HttpResponseValidator {
			handleResponseExceptionWithRequest { exception, _ ->
				val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
				val exceptionResponse = clientException.response
				when(exceptionResponse.status){
					HttpStatusCode.Unauthorized -> throw LoginError()
					HttpStatusCode.BadRequest -> throw InvalidField()
					HttpStatusCode.Conflict -> throw ConflictingUser()
				}
			}
		}
	}

	companion object {
		val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login")
		val mailKey = stringPreferencesKey("email")
		val passwordKey = stringPreferencesKey("password")
		val tokenKey = stringPreferencesKey("token")
	}

	@SuppressLint("SourceLockedOrientationActivity")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		lifecycleScope.launch {
			dataStore.edit { it.clear() }
		}
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent {
			val loginNav = rememberNavController()
			val open by keyboardAsState()
			val (cardPlacement, pt) = if (open) {
				Arrangement.Top to 64.dp
			} else {
				Arrangement.Center to 0.dp
			}
			FlashPlayerRevivalTheme {
				Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					Column(Modifier.padding(top = pt).height(10.dp), verticalArrangement = cardPlacement) {
						Logo()
						NavHost(loginNav, "login"){
//							composable("login") { LoginCard(loginViewModel) { loginNav.navigate("signin") } }
//							composable("signin") { SignInCard { loginNav.navigate("login") } }
						}
					}
				}
			}
		}
	}

	suspend fun connect(mail: String, password: String): Boolean {
		return try {
			val token: LoginResponse = httpClient.post(Auth.Login()) {
				contentType(ContentType.Application.Json)
				setBody(Login(mail, password))
			}.body()
			dataStore.edit { settings ->
				settings[mailKey] = mail
				settings[passwordKey] = password
			}
			finish()
			startActivity(Intent(this, MainActivity::class.java).apply { putExtras(bundleOf("token" to token.token)) })
			true
		}catch (_: LoginError){
			withContext(Dispatchers.Main){
				Toast.makeText(this@LoginActivity, R.string.login_error, Toast.LENGTH_LONG).show()
			}
			false
		}catch (_: InvalidField){
			withContext(Dispatchers.Main){
				Toast.makeText(this@LoginActivity, R.string.invalid_field, Toast.LENGTH_LONG).show()
			}
			false
		}
	}

	suspend fun register(mail: String, password: String, pseudo: String): Boolean{
		return try {
			val token: LoginResponse = httpClient.post(Auth.Register()) {
				contentType(ContentType.Application.Json)
				setBody(Register(mail, password, pseudo))
			}.body()
			dataStore.edit { settings ->
				settings[mailKey] = mail
				settings[passwordKey] = password
			}
			finish()
			startActivity(Intent(this, MainActivity::class.java).apply { putExtras(bundleOf("token" to token.token)) })
			true
		}catch (_: InvalidField){
			withContext(Dispatchers.Main){
				Toast.makeText(this@LoginActivity, R.string.invalid_field, Toast.LENGTH_LONG).show()
			}
			false
		}catch (_: ConflictingUser){
			withContext(Dispatchers.Main){
				Toast.makeText(this@LoginActivity, R.string.conflicting_user, Toast.LENGTH_LONG).show()
			}
			false
		}
	}
}

@Composable
fun Logo() {
	Row(Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
		Image(painterResource(R.drawable.banner), "Banner")
	}
}