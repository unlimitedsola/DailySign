package love.sola.spigot.dailysign.sql

import love.sola.spigot.dailysign.settings
import java.time.LocalDateTime
import java.util.*

data class SignInfo(
    val playerId: UUID,
    val playerName: String,
    val rewardedServer: List<String>,
    val time: LocalDateTime
) {
    fun isRewardedOnThisServer() = rewardedServer.contains(settings.serverGroup)

    fun applyRewardThisServer() = copy(rewardedServer = rewardedServer + settings.serverGroup)
}

