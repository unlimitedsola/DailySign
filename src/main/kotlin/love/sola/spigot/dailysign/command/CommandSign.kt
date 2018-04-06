package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.settings
import love.sola.spigot.dailysign.sql.SignInfo
import love.sola.spigot.dailysign.utils.format
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


fun CommandMain.sign(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    var signInfo: SignInfo? = dao.querySignInfoOfDay(sender)
    val userInfo = dao.queryUserInfo(sender)
    if (signInfo != null) {
        if (!signInfo.isRewardedOnServer(settings.serverGroup)) {
            sender.tellraw(lang("Button_Claim_Reward"))
        }
        sender.sendMessage(
            format(
                "Info_Format",
                userInfo!!.playerName,
                userInfo.continuousSignCount,
                userInfo.continuousSignCount,
                userInfo.signCount
            )
        )
        return true
    }
    dao.sign(sender)
    userInfo!!.signCount = userInfo.signCount + 1
    signInfo = dao.querySignInfoYesterday(sender)
    if (signInfo != null) {
        userInfo.continuousSignCount = userInfo.continuousSignCount + 1
        if (userInfo.continuousSignCount > userInfo.highestContinuous) {
            userInfo.highestContinuous = userInfo.continuousSignCount
        }
    } else {
        userInfo.continuousSignCount = 1
    }
    dao.updateUserInfo(userInfo)
    sender.sendMessage(lang("Sign_Success"))
    sender.sendMessage(
        format(
            "Info_Format",
            userInfo.playerName,
            userInfo.continuousSignCount,
            userInfo.continuousSignCount,
            userInfo.signCount
        )
    )
    sender.tellraw(lang("Button_Claim_Reward"))
    return true
}

