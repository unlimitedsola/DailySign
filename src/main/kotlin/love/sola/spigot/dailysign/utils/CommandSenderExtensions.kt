package love.sola.spigot.dailysign.utils

import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player

fun Player.tellraw(rawJson: String) = spigot().sendMessage(*ComponentSerializer.parse(rawJson))
