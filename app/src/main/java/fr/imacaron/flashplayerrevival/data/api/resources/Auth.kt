package fr.imacaron.flashplayerrevival.data.api.resources

import io.ktor.resources.*

@Resource("/auth")
class Auth {
	@Resource("register")
	class Register(val parent: Auth = Auth())

	@Resource("login")
	class Login(val parent: Auth = Auth())
}