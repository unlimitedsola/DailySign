package love.sola.spigot.dailysign

import love.sola.spigot.dailysign.utils.AutoConfigurationSerializable
import love.sola.spigot.dailysign.utils.getValue
import love.sola.spigot.dailysign.utils.setValue
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class Settings {

    val serverGroup: String by config
    val rewards: Rewards = Rewards(config.getConfigurationSection("rewards"))
    val resignRequirements: ConfigurationSection by config //<Int, Int>
    val resignItem: ItemStack by config
    val signAllItem: ItemStack by config


    class Rewards(subConfig: ConfigurationSection) {

        var generic: List<Reward> by subConfig
        var streak: ConfigurationSection by subConfig //<Int, List<Reward>>

        class Reward(map: MutableMap<String, Any>) : AutoConfigurationSerializable {

            var items: List<ItemStack> by map
            var commands: List<String> by map
            var chance: Int by map

        }
    }
}

