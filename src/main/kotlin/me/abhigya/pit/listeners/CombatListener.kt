package me.abhigya.pit.listeners

import me.abhigya.pit.model.PitPlayer.Companion.toPitPlayer
import me.abhigya.pit.model.RunningArena
import me.abhigya.pit.model.toBlockPos
import me.abhigya.pit.model.toLocation
import me.abhigya.pit.model.toPos3D
import me.abhigya.pit.util.BREAKABLE_BLOCK_METADATA
import me.abhigya.pit.util.hasMetaData
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.time.Instant

class CombatListener(
    private val arena: RunningArena
) : Listener {

    @EventHandler
    fun BlockBreakEvent.handleBlockBreak() {
        if (block.world != arena.world) return
        if (block.hasMetaData(BREAKABLE_BLOCK_METADATA)) return

        isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.handleBlockPlace() {
        if (block.world != arena.world) return
        isCancelled = true
    }

    @EventHandler
    fun PlayerMoveEvent.handlePlayerMove() {
        to ?: return
        if (to!!.world != arena.world) return
        if (from.toBlockPos() == to?.toBlockPos()) return

        val player = player.toPitPlayer()
        if (!player.isInPit) {
            if (from.toPos3D() in arena.data.spawnArea && to!!.toPos3D() !in arena.data.spawnArea) {
                player.isInPit = true
                // Increase stat
            }
        }
    }

    @EventHandler
    fun EntityDamageEvent.handleEntityDamage() {
        if (entity.world != arena.world) return
        val entity = entity
        if (entity !is Player) return

        val player = entity.toPitPlayer()
        if (!player.isInPit) {
            isCancelled = true
            return
        }

        if (entity.health - finalDamage <= 0) {
            entity.teleport(arena.data.spawn.toLocation(entity.world))
            entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.handleEntityDamageByEntity() {
        if (entity.world != arena.world) return
        if (entity !is Player) return
        if (damager !is Player) return

        val player = (entity as Player).toPitPlayer()
        val damager = (damager as Player).toPitPlayer()

        player.lastHitTagged.push(Instant.now() to damager)
    }

    @EventHandler
    fun EntityExplodeEvent.handleEntityExplode() {
        if (entity.world != arena.world) return
        blockList().clear()
    }

    @EventHandler
    fun BlockExplodeEvent.handleBlockExplode() {
        if (block.world != arena.world) return
        blockList().clear()
    }

    @EventHandler
    fun FoodLevelChangeEvent.handleFoodLevelChange() {
        if (entity.world != arena.world) return
        isCancelled = true
    }

    @EventHandler
    fun CraftItemEvent.handleCraftItem() {
        if (whoClicked.world != arena.world) return
        isCancelled = true
    }

    @EventHandler
    fun PlayerItemDamageEvent.handlePlayerItemDamage() {
        if (player.world != arena.world) return
        isCancelled = true
    }

}