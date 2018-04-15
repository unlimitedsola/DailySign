package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.gui.InventoryMenu
import love.sola.spigot.dailysign.gui.openSignGui
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class CommandMain : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            openSignGui(sender as Player)
            return true
        }
        return when (args[0].toLowerCase()) {
            "info" -> ::info
            "top" -> ::rank
            else -> {
                return false
            }
        }(sender, command, label, args)
    }

}
