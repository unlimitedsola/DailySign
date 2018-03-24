package love.sola.spigot.dailysign

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


class Rewarder {

    private val rd = Random()

    fun check(player: Player): Boolean {
        run {
            val reward = randomReward(settings.rewards.generic)
            rewardPlayer(player, reward!!)
        }
        val userInfo = dao.queryUserInfo(player.name)
        run {
            //TODO floor entry
            val rewards = settings.rewards.streak[userInfo!!.continuousSignCount.toString()] ?: return@run
            val reward = randomReward(rewards as List<Settings.Rewards.Reward>)
            rewardPlayer(player, reward!!)
        }
        return true
    }

    private fun randomReward(rewards: List<Settings.Rewards.Reward>): Settings.Rewards.Reward? {
        var chanceTotal = rewards.sumBy { it.chance }
        val random = rd.nextInt(chanceTotal)
        var result: Settings.Rewards.Reward? = null
        chanceTotal = 0
        for (reward in rewards) {
            chanceTotal += reward.chance
            if (chanceTotal > random) {
                result = reward
            }
        }
        return result
    }

    private fun rewardPlayer(player: Player, reward: Settings.Rewards.Reward) {
        for (command in reward.commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.name))
        }
        for (item in reward.items) {
            player.inventory.addItem(item.clone())
        }
    }

}
