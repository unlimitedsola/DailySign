package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.*
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandMain.get(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    val info = dao.querySignInfoOfDay(sender)
    if (info == null) {
        sender.tellraw(lang("Button_Click_Me_To_Sign"))
    } else {
        if (!info.isRewardedOnServer(settings.serverGroup)) {
            if (rewarder.check(sender)) {
                dao.updateSignInfo(info.applyRewardOnServer(settings.serverGroup))
            }
        } else {
            sender.sendMessage(lang("Already_Claimed"))
        }
    }
    return true
}

