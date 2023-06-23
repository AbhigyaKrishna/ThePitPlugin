package me.abhigya.pit.model

import java.util.concurrent.ConcurrentHashMap

data class ArenaData(
    val worldName: String,
    val spawn: Pos3D,
    val spawnArea: BoundingBox,
    val pitHoleArea: BoundingBox,
    val minY: Int
)

val ALL_ARENA_DATA: MutableMap<String, ArenaData> = ConcurrentHashMap()

fun getArenaData(key: String): ArenaData? {
    return ALL_ARENA_DATA[key]
}