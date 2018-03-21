package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.utils.format
import love.sola.spigot.dailysign.utils.lang
import org.bukkit.command.Command
import org.bukkit.command.CommandSender


fun CommandMain.rank(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (args.size < 2) {
        return false
    }
    val type: String = when (args[1]) {
        "all" -> "count"
        "now" -> "continuous"
        "before" -> "highest"
        else -> {
            sender.sendMessage(lang("Invalid_Type"))
            return false
        }
    }
    var page = if (args.size == 3) Integer.parseInt(args[2]) else 1
    page = Math.max(0, page)
    val infoList = dao.queryRankBoard(type, 15 * (page - 1), 15)
    sender.sendMessage(format("Rank_List_Header", page))
    for (info in infoList) {
        sender.sendMessage(
                format(
                        "Rank_List_Entry",
                        info.username,
                        info.signCount,
                        info.continuousSignCount,
                        info.highestContinuous
                )
        )
    }
    return true
}

