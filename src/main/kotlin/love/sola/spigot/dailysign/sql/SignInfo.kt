package love.sola.spigot.dailysign.sql

import java.time.LocalDateTime
import java.util.*

data class SignInfo(
    val playerId: UUID,
    val playerName: String,
    val server: String,
    val time: LocalDateTime
)
