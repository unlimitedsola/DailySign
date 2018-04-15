package love.sola.spigot.dailysign

import love.sola.spigot.dailysign.sql.SignInfo
import love.sola.spigot.dailysign.utils.format
import love.sola.spigot.dailysign.utils.lang
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.LocalTime

fun sign(player: Player) {
    val signInfo: SignInfo? = dao.querySignInfoOfDay(player)
    if (signInfo != null) return
    dao.sign(player)
    dao.updateUserInfo(player)
    player.sendMessage(lang("Sign_Success"))
}

fun resign(player: Player, date: LocalDate) {
    if (dao.querySignInfoOfDay(player, date) != null) {
        return
    }
    val requireCount = settings.getRequiredAmountForResign(date)
    if (requireCount < 0) {
        player.sendMessage(lang("Unresignable"))
        return
    }
    if (!player.inventory.containsAtLeast(settings.resignItem, requireCount)) {
        player.sendMessage(lang("Not_Enough_Requirements"))
    } else {
        val item = settings.resignItem.clone().apply {
            amount = requireCount
        }
        player.inventory.removeItem(item)
        dao.sign(player, date.atTime(LocalTime.now()))
        dao.updateUserInfo(player)
        player.sendMessage(lang("Resign_Success"))
    }
}
