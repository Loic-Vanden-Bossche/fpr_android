package fr.imacaron.flashplayerrevival.api.error

class LoginError: Exception("Login error")

class InvalidField: Exception("Invalid Fields")

class ConflictingUser: Exception("Conflicting User")