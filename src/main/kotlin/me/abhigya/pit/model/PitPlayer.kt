package me.abhigya.pit.model

import org.bukkit.entity.Player
import java.time.temporal.TemporalAccessor
import java.util.*

data class PitPlayer(
    private val player: Player,
) : Player by player {

    companion object {
        private val pitPlayerByUUID = mutableMapOf<UUID, PitPlayer>()

        fun Player.toPitPlayer(): PitPlayer {
            return pitPlayerByUUID.computeIfAbsent(uniqueId) { PitPlayer(this) }
        }

        fun get(uuid: UUID): PitPlayer? {
            return pitPlayerByUUID[uuid]
        }
    }

    var lastHitTagged: Pair<TemporalAccessor, PitPlayer>? = null

}