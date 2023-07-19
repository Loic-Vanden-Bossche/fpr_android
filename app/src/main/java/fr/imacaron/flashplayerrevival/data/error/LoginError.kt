package fr.imacaron.flashplayerrevival.data.error

class LoginError: Exception("Login error")

class InvalidField: Exception("Invalid Fields")

class ConflictingUser: Exception("Conflicting User")