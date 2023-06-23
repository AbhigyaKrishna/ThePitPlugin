package me.abhigya.pit.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import me.abhigya.pit.configuration.Configs
import me.abhigya.pit.support.VoidChunkGenerator
import me.abhigya.pit.util.ext.Bukkit
import me.abhigya.pit.util.ext.setGameRule
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack

data class RunningArena(
    val data: ArenaData,
    val configs: Configs
) {

    var state: ArenaState = ArenaState.WAITING
        private set
    private val players: MutableSet<PitPlayer> = mutableSetOf()
    lateinit var world: World
        private set

    suspend fun init() = coroutineScope {
        state = ArenaState.STARTING
        var wrld = Bukkit.getWorld(data.worldName)
        if (wrld == null) {
            withContext(Dispatchers.Bukkit) {
                val creator = WorldCreator(data.worldName)
                creator.generateStructures(false)
                creator.generator(VoidChunkGenerator)
                wrld = creator.createWorld()
            }
        }

        world = wrld!!

        configs.mainConfig.gameRules().forEach {
            world.setGameRule<Any>(it)
        }

        state = ArenaState.RUNNING
    }

    fun spawn(player: PitPlayer) {
        player.teleport(data.spawn.toLocation(world))
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        player.applicableInventory.applyTo(player)
    }

    fun addPlayer(player: PitPlayer) {
        players.add(player)
        player.applicableInventory.populate(InventoryDataBuilder {
            helmet = ItemStack(Material.IRON_HELMET)
            chestplate = ItemStack(Material.IRON_CHESTPLATE)
            leggings = ItemStack(Material.IRON_LEGGINGS)
            boots = ItemStack(Material.IRON_BOOTS)
            this[0] = ItemStack(Material.IRON_SWORD)
            this[1] = ItemStack(Material.BOW)
            this[8] = ItemStack(Material.ARROW, 32)
        })
        spawn(player)
    }

}

enum class ArenaState {
    WAITING, STARTING, RUNNING, ENDING
}