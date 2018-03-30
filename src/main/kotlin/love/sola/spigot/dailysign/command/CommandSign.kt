package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.settings
import love.sola.spigot.dailysign.sql.SignInfo
import love.sola.spigot.dailysign.utils.format
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.apache.commons.lang.StringUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*


fun CommandMain.sign(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    var signInfo: SignInfo? = dao.querySignInfo(sender)
    val userInfo = dao.queryUserInfo(sender)
    if (signInfo != null) {
        val servers = Arrays.asList(*StringUtils.split(signInfo.server, ";"))
        if (!servers.contains(settings.serverGroup)) {
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
    dao.signNow(sender)
    signInfo = dao.querySignInfoYesterday(sender)
    userInfo!!.signCount = userInfo.signCount + 1
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

