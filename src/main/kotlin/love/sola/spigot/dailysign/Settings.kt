package love.sola.spigot.dailysign

import love.sola.spigot.dailysign.utils.AutoConfigurationSerializable
import love.sola.spigot.dailysign.utils.getValue
import love.sola.spigot.dailysign.utils.setValue
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class Settings {

    val serverGroup: String by config
    var rewards: Rewards by config
    val resignRequirements: ConfigurationSection by config //<Int, Int>
    val resignItem: ItemStack by config

}

class Rewards(map: MutableMap<String, Any?>) : AutoConfigurationSerializable {

    var generic: MutableList<Reward> by map
    var streak: MutableMap<Int, MutableList<Reward>> by map

}

class Reward(map: MutableMap<String, Any?>) : AutoConfigurationSerializable {

    private val _map = map.withDefault { null }
    var items: List<ItemStack>? by _map
    var commands: List<String>? by _map

    var chance: Int by _map

}
