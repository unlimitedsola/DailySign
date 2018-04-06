package love.sola.spigot.dailysign.sql

import java.time.LocalDateTime
import java.util.*

data class SignInfo(
    val playerId: UUID,
    val playerName: String,
    val rewardedServer: List<String>,
    val time: LocalDateTime
) {
    fun isRewardedOnServer(server: String) = rewardedServer.contains(server)

    fun applyRewardOnServer(server: String) = copy(rewardedServer = rewardedServer + server)
}

