package me.abhigya.pit.model

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

// -------------------------------------------- //
// Pos Classes
// -------------------------------------------- //
data class Pos3D(
    val x: Double,
    val y: Double,
    val z: Double
)

data class Pos2D(
    val x: Double,
    val z: Double
)

data class BlockPos(
    val x: Int,
    val y: Int,
    val z: Int
)

// -------------------------------------------- //
// Classes Math
// -------------------------------------------- //

// Pps3D
operator fun Pos3D.plus(other: Pos3D): Pos3D = Pos3D(x + other.x, y + other.y, z + other.z)

operator fun Pos3D.minus(other: Pos3D): Pos3D = Pos3D(x - other.x, y - other.y, z - other.z)

operator fun Pos3D.times(other: Pos3D): Pos3D = Pos3D(x * other.x, y * other.y, z * other.z)

operator fun Pos3D.div(other: Pos3D): Pos3D = Pos3D(x / other.x, y / other.y, z / other.z)

operator fun Pos3D.plus(other: Pos2D): Pos3D = Pos3D(x + other.x, y, z + other.z)

operator fun Pos3D.minus(other: Pos2D): Pos3D = Pos3D(x - other.x, y, z - other.z)

operator fun Pos3D.times(other: Pos2D): Pos3D = Pos3D(x * other.x, y, z * other.z)

operator fun Pos3D.div(other: Pos2D): Pos3D = Pos3D(x / other.x, y, z / other.z)

operator fun Pos3D.plus(other: BlockPos): Pos3D = Pos3D(x + other.x, y + other.y, z + other.z)

operator fun Pos3D.minus(other: BlockPos): Pos3D = Pos3D(x - other.x, y - other.y, z - other.z)

operator fun Pos3D.times(other: BlockPos): Pos3D = Pos3D(x * other.x, y * other.y, z * other.z)

operator fun Pos3D.div(other: BlockPos): Pos3D = Pos3D(x / other.x, y / other.y, z / other.z)

operator fun <T : Number> Pos3D.plus(num: T): Pos3D {
    val n = num.toDouble()
    return Pos3D(x + n, y + n, z + n)
}

operator fun <T : Number> Pos3D.minus(num: T): Pos3D {
    val n = num.toDouble()
    return Pos3D(x - n, y - n, z - n)
}

operator fun <T : Number> Pos3D.times(num: T): Pos3D {
    val n = num.toDouble()
    return Pos3D(x * n, y * n, z * n)
}

operator fun <T : Number> Pos3D.div(num: T): Pos3D {
    val n = num.toDouble()
    return Pos3D(x / n, y / n, z / n)
}

// Pos2D
operator fun Pos2D.plus(other: Pos2D): Pos2D = Pos2D(x + other.x, z + other.z)

operator fun Pos2D.minus(other: Pos2D): Pos2D = Pos2D(x - other.x, z - other.z)

operator fun Pos2D.times(other: Pos2D): Pos2D = Pos2D(x * other.x, z * other.z)

operator fun Pos2D.div(other: Pos2D): Pos2D = Pos2D(x / other.x, z / other.z)

operator fun <T : Number> Pos2D.plus(num: T): Pos2D {
    val n = num.toDouble()
    return Pos2D(x + n, z + n)
}

operator fun <T : Number> Pos2D.minus(num: T): Pos2D {
    val n = num.toDouble()
    return Pos2D(x - n, z - n)
}

operator fun <T : Number> Pos2D.times(num: T): Pos2D {
    val n = num.toDouble()
    return Pos2D(x * n, z * n)
}

operator fun <T : Number> Pos2D.div(num: T): Pos2D {
    val n = num.toDouble()
    return Pos2D(x / n, z / n)
}

// BlockPos
operator fun BlockPos.plus(other: BlockPos): BlockPos = BlockPos(x + other.x, y + other.y, z + other.z)

operator fun BlockPos.minus(other: BlockPos): BlockPos = BlockPos(x - other.x, y - other.y, z - other.z)

operator fun BlockPos.times(other: BlockPos): BlockPos = BlockPos(x * other.x, y * other.y, z * other.z)

operator fun BlockPos.div(other: BlockPos): BlockPos = BlockPos(x / other.x, y / other.y, z / other.z)

operator fun BlockPos.plus(other: Pos2D): BlockPos = BlockPos(x + other.x.toInt(), y, z + other.z.toInt())

operator fun BlockPos.minus(other: Pos2D): BlockPos = BlockPos(x - other.x.toInt(), y, z - other.z.toInt())

operator fun BlockPos.times(other: Pos2D): BlockPos = BlockPos(x * other.x.toInt(), y, z * other.z.toInt())

operator fun BlockPos.div(other: Pos2D): BlockPos = BlockPos(x / other.x.toInt(), y, z / other.z.toInt())

operator fun BlockPos.plus(other: Pos3D): BlockPos = BlockPos(x + other.x.toInt(), y + other.y.toInt(), z + other.z.toInt())

operator fun BlockPos.minus(other: Pos3D): BlockPos = BlockPos(x - other.x.toInt(), y - other.y.toInt(), z - other.z.toInt())

operator fun BlockPos.times(other: Pos3D): BlockPos = BlockPos(x * other.x.toInt(), y * other.y.toInt(), z * other.z.toInt())

operator fun BlockPos.div(other: Pos3D): BlockPos = BlockPos(x / other.x.toInt(), y / other.y.toInt(), z / other.z.toInt())

operator fun <T : Number> BlockPos.plus(num: T): BlockPos {
    val n = num.toInt()
    return BlockPos(x + n, y + n, z + n)
}

operator fun <T : Number> BlockPos.minus(num: T): BlockPos {
    val n = num.toInt()
    return BlockPos(x - n, y - n, z - n)
}

operator fun <T : Number> BlockPos.times(num: T): BlockPos {
    val n = num.toInt()
    return BlockPos(x * n, y * n, z * n)
}

operator fun <T : Number> BlockPos.div(num: T): BlockPos {
    val n = num.toInt()
    return BlockPos(x / n, y / n, z / n)
}

// -------------------------------------------- //
// Pos3D Conversion
// -------------------------------------------- //
fun Pos3D.toPos2D(): Pos2D = Pos2D(x, z)

fun Pos3D.toBlockPos(): BlockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())

// -------------------------------------------- //
// Pos2D Conversion
// -------------------------------------------- //
fun Pos2D.toPos3D(y: Double): Pos3D = Pos3D(x, y, z)

fun Pos2D.toBlockPos(y: Int = 0): BlockPos = BlockPos(x.toInt(), y, z.toInt())

// -------------------------------------------- //
// BlockPos Conversion
// -------------------------------------------- //
fun BlockPos.toPos3D(): Pos3D = Pos3D(x.toDouble(), y.toDouble(), z.toDouble())

fun BlockPos.toPos2D(): Pos2D = Pos2D(x.toDouble(), z.toDouble())

// -------------------------------------------- //
// Bukkit Location Conversion
// -------------------------------------------- //
fun Pos3D.toLocation(world: World? = null): Location = Location(world, x, y, z)

fun Pos2D.toLocation(world: World? = null, y: Double = 0.0): Location = Location(world, x, y, z)

fun BlockPos.toLocation(world: World? = null): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

fun Location.toPos3D(): Pos3D = Pos3D(x, y, z)

fun Location.toPos2D(): Pos2D = Pos2D(x, z)

fun Location.toBlockPos(): BlockPos = BlockPos(blockX, blockY, blockZ)

// -------------------------------------------- //
// Bukkit Vector Conversion
// -------------------------------------------- //
fun Pos3D.toVector(): Vector = Vector(x, y, z)

fun Pos2D.toVector(y: Double = 0.0): Vector = Vector(x, y, z)

fun BlockPos.toVector(): Vector = Vector(x.toDouble(), y.toDouble(), z.toDouble())

fun Vector.toPos3D(): Pos3D = Pos3D(x, y, z)

fun Vector.toPos2D(): Pos2D = Pos2D(x, z)

fun Vector.toBlockPos(): BlockPos = BlockPos(blockX, blockY, blockZ)

// -------------------------------------------- //
// Extensions
// -------------------------------------------- //
fun Pos3D.isInfinite(): Boolean = x.isInfinite() || y.isInfinite() || z.isInfinite()

fun Pos3D.isFinite(): Boolean = x.isFinite() && y.isFinite() && z.isFinite()

fun Pos3D.isNaN(): Boolean = x.isNaN() || y.isNaN() || z.isNaN()

fun Pos3D.isZero(): Boolean = x == 0.0 && y == 0.0 && z == 0.0

fun Pos2D.isInfinite(): Boolean = x.isInfinite() || z.isInfinite()

fun Pos2D.isFinite(): Boolean = x.isFinite() && z.isFinite()

fun Pos2D.isNaN(): Boolean = x.isNaN() || z.isNaN()

fun Pos2D.isZero(): Boolean = x == 0.0 && z == 0.0

fun BlockPos.isZero(): Boolean = x == 0 && y == 0 && z == 0