package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.settings
import love.sola.spigot.dailysign.utils.format
import love.sola.spigot.dailysign.utils.lang
import love.sola.spigot.dailysign.utils.tellraw
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*


private val confirmSet: MutableSet<Player> = HashSet()

fun CommandMain.resign(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    val signInfo = dao.querySignInfoOfDay(sender)
    if (signInfo == null) {
        sender.tellraw(lang("Button_Click_Me_To_Sign"))
        return true
    }
    val userInfo = dao.queryUserInfo(sender)!!
    var requireCount = -1
    for ((key, value) in settings.resignRequirements.getValues(false)) {
        if (userInfo.continuousSignCount <= key.toInt()) {
            requireCount = value as Int
        }
    }
    if (requireCount < 0) {
        sender.sendMessage(lang("Unresignable"))
        return true
    }
    if (!confirmSet.contains(sender)) {
        sender.sendMessage(format("Resign_Confirm", requireCount))
        confirmSet.add(sender)
    } else {
        if (!sender.inventory.containsAtLeast(settings.resignItem, requireCount)) {
            sender.sendMessage(lang("Not_Enough_Requirements"))
        } else {
            val item = settings.resignItem.clone()
            item.amount = requireCount
            sender.inventory.removeItem(item)
            dao.signByOffset(sender, userInfo.continuousSignCount * -1)
            userInfo.continuousSignCount = dao.recountContinuous(sender)
            if (userInfo.continuousSignCount > userInfo.highestContinuous) {
                userInfo.highestContinuous = userInfo.continuousSignCount
            }
            dao.updateUserInfo(userInfo)
            sender.sendMessage(lang("Resign_Success"))
        }
        confirmSet.remove(sender)
    }
    return true
}

