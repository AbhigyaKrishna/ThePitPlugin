package me.abhigya.pit.model

import org.bukkit.entity.Player
import java.time.temporal.TemporalAccessor
import java.util.*

data class PitPlayer(
    private val player: Player,
    val stats: PlayerStats = PlayerStats(),
    val applicableInventory: InventoryData = InventoryData()
) : Player by player {

    companion object {
        private val pitPlayerByUUID: MutableMap<UUID, PitPlayer> = mutableMapOf()

        fun Player.toPitPlayer(): PitPlayer {
            return pitPlayerByUUID.computeIfAbsent(uniqueId) { PitPlayer(this) }
        }

        operator fun get(uuid: UUID): PitPlayer? {
            return pitPlayerByUUID[uuid]
        }
    }

    var lastHitTagged: Stack<HitData> = Stack()
    var isInPit: Boolean = false
        internal set

}

typealias HitData = Pair<TemporalAccessor, PitPlayer>