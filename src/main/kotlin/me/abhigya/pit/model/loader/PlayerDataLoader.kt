package me.abhigya.pit.model.loader

import me.abhigya.jooq.codegen.tables.references.PLAYERS
import me.abhigya.jooq.codegen.tables.references.STATS
import me.abhigya.pit.database.transaction
import me.abhigya.pit.model.Balance
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
            val record = fetch(PLAYERS, PLAYERS.UUID.eq(player.uniqueId)).first()
            player.balance.set(record.balance?.run(Balance::fromNumber) ?: Balance.zero())
            player.bounty = record.bounty ?: 0
            player.level = record.level ?: 0
            player.renown = record.renown ?: 0
            status = LoadStatus.LOADED_PLAYER_DATA

            val stats = fetch(STATS, STATS.UUID.eq(player.uniqueId)).first()
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