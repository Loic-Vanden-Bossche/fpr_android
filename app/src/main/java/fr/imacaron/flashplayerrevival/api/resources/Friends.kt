package fr.imacaron.flashplayerrevival.api.resources

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Resource("/friends")
class Friends{
    @Resource("pending")
    class Pendind(val parent: Friends = Friends())

    @Resource("{id}")
    class Id(val parent: Friends = Friends(), @Serializable(with = UUIDSerializer::class)val id: UUID)
}
