package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.settings
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


fun CommandMain.signAll(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    val p = sender
    val signInfo = dao.querySignInfo(p.name)
    if (signInfo == null) {
        sender.tellraw(lang("Button_Click_Me_To_Sign"))
        return true
    }
    val userInfo = dao.queryUserInfo(p.name)
    if (!p.inventory.containsAtLeast(
            settings.signAllItem,
            settings.signAllItem.amount
    )) {
        p.sendMessage(lang("Not_Enough_Requirements"))
    } else {
        p.inventory.removeItem(settings.signAllItem)
        dao.signAll(p.name)
        userInfo!!.signCount = dao.querySignCount(p.name)
        userInfo.continuousSignCount = userInfo.signCount
        userInfo.highestContinuous = userInfo.signCount
        dao.updateUserInfo(userInfo)
        p.sendMessage(lang("Sign_All_Success"))
    }
    return true
}


