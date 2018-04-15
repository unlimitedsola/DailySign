package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.utils.format
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


fun CommandMain.info(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    val signInfo = dao.querySignInfoOfDay(sender)
    if (signInfo == null) {
        sender.tellraw(lang("Button_Click_Me_To_Sign"))
    }
    val userInfo = dao.queryUserInfo(sender)
    sender.sendMessage(
            format(
                    "Info_Format",
                    userInfo!!.playerName,
                    userInfo.signCount,
                    userInfo.continuousSignCount,
                    userInfo.highestContinuous
            ).split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    )
    return true
}

