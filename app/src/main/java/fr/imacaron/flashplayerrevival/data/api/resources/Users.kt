package fr.imacaron.flashplayerrevival.data.api.resources

import io.ktor.resources.*

@Resource("/users")
class Users {
    @Resource("search/{search}")
    class Search(val parent: Users = Users(), val search: String)
}