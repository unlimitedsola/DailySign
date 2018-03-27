package love.sola.spigot.dailysign

import love.sola.spigot.dailysign.command.CommandMain
import love.sola.spigot.dailysign.listener.PlayerListener
import love.sola.spigot.dailysign.sql.Dao
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin


internal lateinit var plugin: DailySign
    private set
internal val config: FileConfiguration
    get() = plugin.config
internal lateinit var dao: Dao
    private set
internal lateinit var settings: Settings
    private set
internal lateinit var rewarder: Rewarder
    private set

class DailySign : JavaPlugin() {

    override fun onEnable() {
        plugin = this
        ConfigurationSerialization.registerClass(Reward::class.java)
        ConfigurationSerialization.registerClass(Rewards::class.java)
        settings = Settings()
        dao = Dao()
        rewarder = Rewarder()
        this.getCommand("qiandao").executor = CommandMain()
        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)
    }

}
