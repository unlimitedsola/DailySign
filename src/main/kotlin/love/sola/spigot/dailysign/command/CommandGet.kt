package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.*
import love.sola.spigot.dailysign.sql.Dao
import love.sola.spigot.dailysign.sql.SignInfo
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.apache.commons.lang.StringUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandMain.get(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    val info = dao.querySignInfo(sender.name)
    if (info == null) {
        sender.tellraw(lang("Button_Click_Me_To_Sign"))
    } else {
        val servers = StringUtils.split(info.server, ";")
        if (!servers.contains(settings.serverGroup)) {
            if (rewarder.check(sender)) {
                val rewardServer = if (info.server == "") {
                    settings.serverGroup
                } else {
                    info.server + ";" + settings.serverGroup
                }
                dao.updateRewarded(SignInfo(info.username, rewardServer, info.time))
            }
        } else {
            sender.sendMessage(lang("Already_Claimed"))
        }
    }
    return true
}

