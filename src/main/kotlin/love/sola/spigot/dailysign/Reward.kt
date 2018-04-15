package love.sola.spigot.dailysign

import org.bukkit.entity.Player


fun reward(player: Player): Boolean {
    val signInfo = dao.querySignInfoOfDay(player) ?: return false
    if (signInfo.isRewardedOnThisServer()) return false
    dao.updateSignInfo(signInfo.applyRewardThisServer())
    settings.rewardSetting.generic.rewardPlayer(player)
    val userInfo = dao.queryUserInfo(player)
    settings.rewardSetting.streak.entries.firstOrNull {
        it.key <= userInfo!!.continuousSignCount
    }?.value?.rewardPlayer(player)
    return true
}



