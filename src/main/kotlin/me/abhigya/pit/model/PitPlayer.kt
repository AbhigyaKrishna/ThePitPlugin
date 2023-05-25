package me.abhigya.pit.model

import org.bukkit.entity.Player
import java.time.temporal.TemporalAccessor
import java.util.*

data class PitPlayer(
    private val player: Player,
    val balance: Balance = Balance.zero()
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

    var lastHitTagged: Pair<TemporalAccessor, PitPlayer>? = null

}