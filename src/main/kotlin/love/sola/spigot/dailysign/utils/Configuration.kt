package love.sola.spigot.dailysign.utils

import love.sola.spigot.dailysign.config
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.text.MessageFormat
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

operator fun <R, T> ConfigurationSection.getValue(thisRef: R, property: KProperty<*>): T = get(property.name) as T
operator fun <R, T> ConfigurationSection.setValue(thisRef: R, property: KProperty<*>, value: T) =
    set(property.name, value)

interface AutoConfigurationSerializable : ConfigurationSerializable {
    override fun serialize(): Map<String, Any?> {
        return this::class.memberProperties.filter { it.visibility != KVisibility.PRIVATE }
            .associate {
                it.name to it.getter.call(this)
            }
    }
}

private val messages: ConfigurationSection by config

fun lang(key: String) = messages.getString(key) ?: "!!$key!!"

private val formatCache: MutableMap<String, MessageFormat> = hashMapOf()

fun format(key: String, vararg args: Any): String {
    return formatCache.computeIfAbsent(key) { MessageFormat(lang(it)) }.format(args)
}
