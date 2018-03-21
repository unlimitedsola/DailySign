package love.sola.spigot.dailysign.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class CommandMain : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return sign(sender, command, label, args)
        }
        return when (args[0].toLowerCase()) {
            "get" -> ::get
            "info" -> ::info
            "top" -> ::rank
            "bq" -> ::resign
            "lq" -> ::signAll
            else -> {
                return false
            }
        }(sender, command, label, args)
    }

}
