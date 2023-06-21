package me.abhigya.pit.model

import org.bukkit.Bukkit
import org.bukkit.World

data class RunningArena(
    val data: ArenaData
) {

    private val players: MutableSet<PitPlayer> = mutableSetOf()
    val world: World = Bukkit.getWorld(data.worldName)!!

    fun addPlayer(player: PitPlayer) {
        players.add(player)
    }

}