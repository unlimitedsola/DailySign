package love.sola.spigot.dailysign.sql

import java.util.*

data class UserInfo(
    val player_id: UUID,
    val playerName: String,
    var signCount: Long,
    var continuousSignCount: Long,
    var highestContinuous: Long
)
