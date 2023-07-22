package fr.imacaron.flashplayerrevival.state.viewmodel

import androidx.lifecycle.ViewModel
import fr.imacaron.flashplayerrevival.data.api.ApiService
import fr.imacaron.flashplayerrevival.data.dto.out.UserResponse
import io.ktor.client.plugins.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.InputStream

class ProfileViewModel(
	val self: UserResponse?,
	val drawerViewModel: DrawerViewModel
): ViewModel() {
	suspend fun save(input: InputStream, mimeType: String){
		ApiService.httpClient.submitFormWithBinaryData("/api/users/picture", formData {
			append("file", input.readBytes(), Headers.build {
				append(HttpHeaders.ContentType, mimeType)
				append(HttpHeaders.ContentDisposition, "filename=\"file\"")
			})
		}){
			onUpload { a, b ->
				println("Uploading $a, $b")
			}
		}
		input.close()
	}
}