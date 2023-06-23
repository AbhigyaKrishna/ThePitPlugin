package me.abhigya.pit.support

import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import java.util.*

object VoidChunkGenerator : ChunkGenerator() {
    override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData {
        return createChunkData(world)
    }
}