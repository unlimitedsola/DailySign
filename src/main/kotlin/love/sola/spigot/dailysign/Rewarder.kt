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
            val rewards = settings.rewards.streak.entries.firstOrNull {
                it.key <= userInfo!!.continuousSignCount
            }?.value ?: return@run
            val reward = randomReward(rewards)
            rewardPlayer(player, reward!!)
        }
        return true
    }

    private fun randomReward(rewards: List<Reward>): Reward? {
        var chanceTotal = rewards.sumBy { it.chance }
        val random = rd.nextInt(chanceTotal)
        var result: Reward? = null
        chanceTotal = 0
        for (reward in rewards) {
            chanceTotal += reward.chance
            if (chanceTotal > random) {
                result = reward
            }
        }
        return result
    }

    private fun rewardPlayer(player: Player, reward: Reward) {
        if (reward.commands != null) {
            for (command in reward.commands!!) { // we ignore atomic issue here
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.name))
            }
        }
        if (reward.items != null) {
            for (item in reward.items!!) { // we ignore atomic issue here
                player.inventory.addItem(item.clone())
            }
        }
    }


}
