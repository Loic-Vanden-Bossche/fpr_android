package fr.imacaron.flashplayerrevival.screen.search

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import fr.imacaron.flashplayerrevival.R
import fr.imacaron.flashplayerrevival.state.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.InputStream

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel){
	var imageURI: Uri? by remember { mutableStateOf(null) }
	val context = LocalContext.current
	var bitmap by remember {
		mutableStateOf<Bitmap?>(null)
	}
	val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
		imageURI = uri
	}
	Scaffold(
		topBar = { TopBar(stringResource(R.string.profile), profileViewModel) {
			return@TopBar imageURI?.let { context.contentResolver.openInputStream(it) to context.contentResolver.getType(it) }
		} }) { pv ->
		Surface(
			Modifier.fillMaxSize().padding(pv),
			color = MaterialTheme.colorScheme.background
		) {
			Column {
				ElevatedButton({
					launcher.launch("image/*")
				}) {
					Text(stringResource(R.string.pick_image))
				}
				imageURI?.let {
					val source = ImageDecoder.createSource(context.contentResolver, it)
					bitmap = ImageDecoder.decodeBitmap(source)
					bitmap?.let { btm ->
						Surface(Modifier.padding(16.dp), shape = MaterialTheme.shapes.extraLarge){
							Image(
								bitmap = btm.asImageBitmap(),
								"Profile",
								Modifier.fillMaxWidth().aspectRatio(1f),
								contentScale = ContentScale.Crop,
							)
						}
					}
				} ?: run {
					AsyncImage(
						ImageRequest.Builder(context)
							.data("https://medias.flash-player-revival.net/p/${profileViewModel.self?.id}")
							.memoryCachePolicy(CachePolicy.DISABLED)
							.networkCachePolicy(CachePolicy.DISABLED)
							.build(),
						"Default profile"
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, profileViewModel: ProfileViewModel, save: () -> Pair<InputStream?, String?>? ){
	val scope = rememberCoroutineScope()
	TopAppBar(
		{ Text(title) },
		navigationIcon = {
			IconButton( { scope.launch { profileViewModel.drawerViewModel.navigateHome() } } ){
				Icon(Icons.Default.ArrowBack, "Back")
			}
		},
		actions = {
			IconButton({ scope.launch {
				val data = save()
				data?.let {d ->
					d.first?.let { f ->
						d.second?.let { s ->
							profileViewModel.save(f, s)
						}
					}
				}
			} }) {
				Icon(Icons.Default.Save, "Save")
			}
		}
	)
}