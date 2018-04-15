package love.sola.spigot.dailysign

import love.sola.spigot.dailysign.utils.AutoConfigurationSerializable
import love.sola.spigot.dailysign.utils.getValue
import love.sola.spigot.dailysign.utils.setValue
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Settings {

    val serverGroup: String by config
    var rewardSetting: RewardSetting by config
    val resignRequirements: ConfigurationSection by config //<Int, Int>
    val resignItem: ItemStack by config

    fun getRequiredAmountForResign(date: LocalDate): Int {
        val daysPassed = ChronoUnit.DAYS.between(date, LocalDate.now())
        if (daysPassed < 1) return -1
        for ((key, value) in resignRequirements.getValues(false)) {
            if (daysPassed <= key.toInt()) {
                return value as Int
            }
        }
        return -1
    }

}

@SerializableAs("DailySign.RewardSetting")
class RewardSetting(map: Map<String, Any>) : AutoConfigurationSerializable {

    val generic: RewardWithBonus by map
    val streak: Map<Int, RewardWithBonus> by map

}

@SerializableAs("DailySign.RewardWithBonus")
class RewardWithBonus(map: Map<String, Any>) : AutoConfigurationSerializable {

    private val reward: RewardProvider by map
    private val bonus: Map<String, RewardProvider> by map

    fun rewardPlayer(player: Player) {
        reward.getReward()?.applyReward(player)
        bonus.forEach {
            if (player.hasPermission(it.key)) {
                it.value.getReward()?.applyReward(player)
            }
        }
    }
}

interface RewardProvider {
    fun getReward(): Reward?
}

@SerializableAs("DailySign.RewardPool")
class RewardPool(map: Map<String, Any>) : AutoConfigurationSerializable, RewardProvider {

    private val rewards: Map<Int, Reward> by map

    override fun getReward(): Reward? {
        val totalWeight = rewards.keys.sum()
        if (totalWeight <= 0) return null
        val randomChance = random.nextInt(totalWeight)
        var chanceTotal = 0
        for (entry in rewards.entries) {
            chanceTotal += entry.key
            if (chanceTotal > randomChance) {
                return entry.value
            }
        }
        return null
    }

}


@SerializableAs("DailySign.Reward")
class Reward(map: Map<String, Any?>) : AutoConfigurationSerializable, RewardProvider {

    private val _map = map.withDefault { null }

    private val items: List<ItemStack>? by _map
    private val commands: List<String>? by _map

    override fun getReward(): Reward? = this

    fun applyReward(player: Player) {
        if (commands != null) {
            for (command in commands!!) { // we ignore atomic issue here
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.name))
            }
        }
        if (items != null) {
            for (item in items!!) { // we ignore atomic issue here
                player.inventory.addItem(item.clone())
            }
        }
    }

}
