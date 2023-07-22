package fr.imacaron.flashplayerrevival.data.api.resources

import io.ktor.resources.*

@Resource("/users")
class Users {
    @Resource("picture")
    class Picture(val parent: Users = Users())

    @Resource("search/{search}")
    class Search(val parent: Users = Users(), val search: String)
}