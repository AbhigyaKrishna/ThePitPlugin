package me.abhigya.pit.listeners

import me.abhigya.pit.ArenaScope
import me.abhigya.pit.model.PitPlayer.Companion.toPitPlayer
import me.abhigya.pit.util.BREAKABLE_BLOCK_METADATA
import me.abhigya.pit.util.hasMetaData
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
import java.time.Instant

@ArenaScope
class CombatListener : Listener {

    @EventHandler
    fun BlockBreakEvent.handleBlockBreak() {
        if (block.hasMetaData(BREAKABLE_BLOCK_METADATA)) return

        isCancelled = true
    }

    @EventHandler
    fun BlockPlaceEvent.handleBlockPlace() {
        isCancelled = true
    }

    @EventHandler
    fun EntityDamageEvent.handleEntityDamage() {
        val entity = entity
        if (entity !is Player) return

        if (entity.health - finalDamage <= 0) {
            TODO("kill and teleport")
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.handleEntityDamageByEntity() {
        if (entity !is Player) return
        if (damager !is Player) return

        val player = (entity as Player).toPitPlayer()
        val damager = (damager as Player).toPitPlayer()

        player.lastHitTagged = Instant.now() to damager
    }

    @EventHandler
    fun EntityExplodeEvent.handleEntityExplode() {
        blockList().clear()
    }

    @EventHandler
    fun BlockExplodeEvent.handleBlockExplode() {
        blockList().clear()
    }

    @EventHandler
    fun FoodLevelChangeEvent.handleFoodLevelChange() {
        isCancelled = true
    }

    @EventHandler
    fun CraftItemEvent.handleCraftItem() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerItemDamageEvent.handlePlayerItemDamage() {
        isCancelled = true
    }

}