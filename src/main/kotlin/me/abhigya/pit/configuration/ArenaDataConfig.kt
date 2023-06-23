package me.abhigya.pit.configuration

import me.abhigya.pit.model.Pos3D
import space.arim.dazzleconf.annote.ConfKey

interface ArenaDataConfig {

    fun key(): String

    @ConfKey("display-name")
    fun displayName(): String

    fun world(): String

    fun spawn(): Pos3D

    @ConfKey("spawn-area")
    fun spawnArea(): BoundingBoxConfig

    @ConfKey("pit-hole-area")
    fun pitHoleArea(): BoundingBoxConfig

    @ConfKey("min-y")
    fun minY(): Int

    interface BoundingBoxConfig {

        fun min(): Pos3D

        fun max(): Pos3D

    }

}