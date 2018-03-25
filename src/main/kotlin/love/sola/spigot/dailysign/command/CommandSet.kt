package love.sola.spigot.dailysign.command

import love.sola.spigot.dailysign.Reward
import love.sola.spigot.dailysign.plugin
import love.sola.spigot.dailysign.settings
import love.sola.spigot.dailysign.utils.lang
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandMain.set(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (!sender.isOp || sender !is Player) {
        sender.sendMessage(lang("Command_Player_Only"))
        return true
    }
    if (args.size < 3) {
        return false
    }
    val type = args[1]
    val chance = Integer.parseInt(args[2])
    val streak = if (args.size == 4) Integer.parseInt(args[3]) else 0
    when (type) {
        "generic" -> insertGenericReward(Reward(hashMapOf()).apply {
            this.chance = chance
            this.items = listOfNotNull(*sender.inventory.storageContents)
        })
        "streak" -> insertStreakReward(
            streak,
            Reward(hashMapOf()).apply {
                this.chance = chance
                this.items = listOfNotNull(*sender.inventory.storageContents)
            })
    }
    plugin.saveConfig()
    sender.sendMessage(lang("Set_Success"))
    return true
}

private fun insertGenericReward(reward: Reward) {
    settings.rewards.generic = settings.rewards.generic.apply { add(reward) }
}


private fun insertStreakReward(streak: Int, reward: Reward) {
    settings.rewards.streak[streak.toString()] =
            (settings.rewards.streak[streak.toString()] as MutableList<Reward>)
                .apply { add(reward) }
}
