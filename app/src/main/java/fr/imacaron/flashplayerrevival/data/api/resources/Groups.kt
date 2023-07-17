package fr.imacaron.flashplayerrevival.data.api.resources

import fr.imacaron.flashplayerrevival.utils.serializer.UUIDSerializer
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import java.util.*

@Resource("/groups")
class Groups {
    @Resource("{id}")
    class Id(val parent: Groups = Groups(), @Serializable(with = UUIDSerializer::class) val id: UUID){
        @Resource("messages")
        class Messages(val parent: Groups.Id, val page: Int = 0, val size: Int = 0) {
            @Resource("{id}")
            class Id(val parent: Messages, @Serializable(with = UUIDSerializer::class) val id: UUID)
        }
    }
}