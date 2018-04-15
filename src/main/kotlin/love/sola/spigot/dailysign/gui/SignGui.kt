package love.sola.spigot.dailysign.gui

import love.sola.spigot.dailysign.dao
import love.sola.spigot.dailysign.resign
import love.sola.spigot.dailysign.reward
import love.sola.spigot.dailysign.sign
import love.sola.spigot.dailysign.utils.lang
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

private val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd EEE")
fun openSignGui(player: Player, month: YearMonth = YearMonth.now()) {
    fun reopen() = openSignGui(player, month)
    val today = LocalDate.now()
    val signInfoMap = dao.querySignInfoOfMonth(player, month).associateBy { it.time.dayOfMonth }
    val menu = inventoryMenu(lang("Gui_Inventory_Name"), 6) {
        if (signInfoMap[today.dayOfMonth]?.isRewardedOnThisServer() == false) {
            icon(2, 0) {
                icon = ItemStack(Material.CHEST).apply {
                    itemMeta = itemMeta.apply { displayName = lang("Gui_Get_Reward") }
                }
                onClick {
                    reward(player)
                    reopen()
                }
            }
        }
        for (dayOfMonth in 1..month.atEndOfMonth().dayOfMonth) {
            val date = month.atDay(dayOfMonth)
            val weekOfMonth = date.get(WeekFields.ISO.weekOfMonth())
            icon(weekOfMonth, date.dayOfWeek.value + 1) {
                if (date == today && dayOfMonth !in signInfoMap) {
                    icon = ItemStack(Material.WATCH).apply {
                        itemMeta = itemMeta.apply { displayName = lang("Gui_Sign") }
                        onClick {
                            sign(player)
                            reopen()
                        }
                    }
                } else {
                    icon = ItemStack(Material.STAINED_GLASS_PANE).apply {
                        itemMeta = itemMeta.apply { displayName = month.atDay(dayOfMonth).format(dateFormat) }
                        amount = dayOfMonth
                        if (dayOfMonth in signInfoMap) {
                            durability = DyeColor.GREEN.woolData.toShort()
                        } else {
                            durability = DyeColor.RED.woolData.toShort()
                            onClick {
                                resign(player, date)
                                reopen()
                            }
                        }
                    }
                }
            }
        }
        icon(0, 0) {
            icon = ItemStack(Material.PAPER).apply {
                itemMeta = itemMeta.apply { displayName = lang("Gui_Prev_Month") }
            }
            onClick {
                openSignGui(player, month.minusMonths(1))
            }
        }
        icon(5, 0) {
            icon = ItemStack(Material.PAPER).apply {
                itemMeta = itemMeta.apply { displayName = lang("Gui_Next_Month") }
            }
            onClick {
                openSignGui(player, month.plusMonths(1))
            }
        }
    }
    menu.open(player)
}
