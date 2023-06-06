package me.abhigya.pit.model.loader

import me.abhigya.pit.database.databaseTransaction
import me.abhigya.pit.database.fetchPlayerData
import me.abhigya.pit.database.fetchPlayerStats
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
        databaseTransaction {
            val record = fetchPlayerData(it, player.uniqueId)
            player.balance.set(record.balance?.run(Balance::fromNumber) ?: Balance.zero())
            player.bounty = record.bounty ?: 0
            player.level = record.level ?: 0
            player.renown = record.renown ?: 0
            status = LoadStatus.LOADED_PLAYER_DATA

            val stats = fetchPlayerStats(it, player.uniqueId)
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