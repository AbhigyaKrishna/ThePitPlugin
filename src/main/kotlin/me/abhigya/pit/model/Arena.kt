package me.abhigya.pit.model

import org.bukkit.World

data class Arena(
    val world: World
) {

    private val players: MutableSet<PitPlayer> = mutableSetOf()

}