package me.abhigya.pit.model

data class ArenaData(
    val worldName: String,
    val spawn: Pos3D,
    val spawnArena: BoundingBox,
    val pitHoleArea: BoundingBox,
    val minY: Int
)