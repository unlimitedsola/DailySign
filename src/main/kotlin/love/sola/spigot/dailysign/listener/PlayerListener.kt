package love.sola.spigot.dailysign.listener

import love.sola.spigot.dailysign.DailySign
import love.sola.spigot.dailysign.dao
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class PlayerListener : Listener {

    @EventHandler
    fun onJoin(evt: PlayerJoinEvent) {
        dao.createNewUser(evt.player)
    }

}
