package me.abhigya.pit.model.loader

import me.abhigya.jooq.codegen.tables.references.PLAYER_STATS
import me.abhigya.pit.database.transaction
import me.abhigya.pit.model.PitPlayer
import me.abhigya.pit.model.PlayerStats

abstract class PlayerDataLoader(
    val player: PitPlayer,
    private val onComplete: PlayerDataLoader.() -> Unit = {}
) {

    abstract fun load()

    protected fun finish() {
        onComplete(this)
    }

}

class LazyPlayerDataLoader(player: PitPlayer) : PlayerDataLoader(player) {

    var status: LoadStatus = LoadStatus.STARTING
        private set

    override fun load() {
        transaction {
            status = LoadStatus.LOADED_PLAYER_DATA
            val stats = fetch(PLAYER_STATS, PLAYER_STATS.UUID.eq(player.uniqueId)).first()
            for (value in PlayerStats.VALUES) {
                player.stats[value] = stats.getValue(value.key(), value.type) ?: value.zeroValue()
            }
            status = LoadStatus.LOADED_PLAYER_STATS

            status = LoadStatus.FINISHED
            finish()
        }
    }

    enum class LoadStatus {
        STARTING,
        LOADED_PLAYER_DATA,
        LOADED_PLAYER_STATS,
        FINISHED
    }
}