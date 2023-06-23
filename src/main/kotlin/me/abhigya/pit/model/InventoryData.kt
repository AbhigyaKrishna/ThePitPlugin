package me.abhigya.pit.model

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class InventoryData : Iterable<ItemStack?> {

    val items: Array<ItemStack?> = arrayOfNulls(41)
    var offHand: ItemStack?
        get() = items[40]
        set(value) {
            items[40] = value
        }
    var helmet: ItemStack?
        get() = items[39]
        set(value) {
            items[39] = value
        }
    var chestplate: ItemStack?
        get() = items[38]
        set(value) {
            items[38] = value
        }
    var leggings: ItemStack?
        get() = items[37]
        set(value) {
            items[37] = value
        }
    var boots: ItemStack?
        get() = items[36]
        set(value) {
            items[36] = value
        }

    var armor: Array<ItemStack?>
        get() = arrayOf(helmet, chestplate, leggings, boots)
        set(value) {
            helmet = value[0]
            chestplate = value[1]
            leggings = value[2]
            boots = value[3]
        }

    fun applyTo(player: Player, clear: Boolean = true) {
        if (clear) {
            player.inventory.clear()
        }

        items.forEachIndexed { index, item ->
            player.inventory.setItem(index, item)
        }
    }

    operator fun get(slot: Int): ItemStack? {
        return items[slot]
    }

    operator fun set(slot: Int, item: ItemStack?) {
        items[slot] = item
    }

    fun add(item: ItemStack) {
        var next = 0
        while (items[next]?.type?.isAir != false) {
            next++
        }
        set(next, item)
    }

    fun removeItem(slot: Int) {
        items[slot] = null
    }

    override fun iterator(): Iterator<ItemStack?> {
        return items.iterator()
    }

    fun populate(other: InventoryData) {
        other.items.forEachIndexed { index, item ->
            items[index] = item
        }
    }

}

@Suppress("FunctionName")
fun InventoryDataBuilder(builder: InventoryData.() -> Unit): InventoryData {
    val data = InventoryData()
    data.builder()
    return data
}