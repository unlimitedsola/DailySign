package love.sola.spigot.dailysign.gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class InventoryMenu(name: String, rows: Int) : InventoryHolder {

    private val inventory = Bukkit.createInventory(this, rows * 9, name)
    private val slotHandlers = arrayOfNulls<(InventoryClickEvent) -> Unit>(rows * 9)
    private var closeHandler: (InventoryCloseEvent) -> Unit = {}

    override fun getInventory(): Inventory = inventory

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun clear() {
        inventory.clear()
        slotHandlers.fill(null)
    }

    fun icon(row: Int, col: Int, op: IconBuilder.() -> Unit) = icon(row * 9 + col, op)

    fun icon(slot: Int, op: IconBuilder.() -> Unit) {
        val builder = IconBuilder()
        op(builder)
        inventory.setItem(slot, builder.icon)
        slotHandlers[slot] = builder.clickHandler
    }

    class IconBuilder {
        var icon: ItemStack? = null
        var clickHandler: ((InventoryClickEvent) -> Unit)? = null
        fun onClick(handler: (InventoryClickEvent) -> Unit) {
            clickHandler = handler
        }
    }

    fun onClose(handler: (InventoryCloseEvent) -> Unit) {
        closeHandler = handler
    }

    private fun onClick(event: InventoryClickEvent) {
        slotHandlers.getOrNull(event.rawSlot)?.invoke(event)
    }

    private fun onClose(event: InventoryCloseEvent) {
        closeHandler(event)
    }

    object MenuListener : Listener {

        @EventHandler(ignoreCancelled = true)
        fun onClick(event: InventoryClickEvent) {
            val holder = event.inventory.holder as? InventoryMenu ?: return
            event.isCancelled = true
            holder.onClick(event)
        }

        @EventHandler(ignoreCancelled = true)
        fun onClose(event: InventoryCloseEvent) {
            val holder = event.inventory.holder as? InventoryMenu ?: return
            holder.onClose(event)
        }

    }

}

fun inventoryMenu(name: String, rows: Int, op: InventoryMenu.() -> Unit): InventoryMenu {
    val menu = InventoryMenu(name, rows)
    op(menu)
    return menu
}
