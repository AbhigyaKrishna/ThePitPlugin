package me.abhigya.pit.model.loader

import me.abhigya.pit.model.PitPlayer

abstract class PlayerDataLoader(val player: PitPlayer) {

    abstract fun load()

}

class LazyPlayerDataLoader(player: PitPlayer) : PlayerDataLoader(player) {

    var status: LoadStatus = LoadStatus.STARTING
        private set

    override fun load() {

    }

    enum class LoadStatus {
        STARTING,
        LOADED_PLAYER_DATA,
        LOADED_PLAYER_STATS,
        FINISHED
    }
}