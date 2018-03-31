package love.sola.spigot.dailysign.sql

import java.util.*

data class SignInfo(
    val playerId: UUID,
    val playerName: String,
    val server: String,
    val time: Int
)
