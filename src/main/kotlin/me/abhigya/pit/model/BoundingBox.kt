package me.abhigya.pit.model

import org.bukkit.World
import org.bukkit.block.Block
import kotlin.math.abs

/**
 * Constructs the new bounding box using the provided minimum and maximum Pos3D.
 *
 * @param minimum the minimum Pos3D.
 * @param maximum the maximum Pos3D.
 */
class BoundingBox(minimum: Pos3D, maximum: Pos3D) {

    companion object {
        val ZERO: BoundingBox = BoundingBox(Pos3D(0.0, 0.0, 0.0), Pos3D(0.0, 0.0, 0.0))
        val BLOCK: BoundingBox = BoundingBox(Pos3D(0.0, 0.0, 0.0), Pos3D(1.0, 1.0, 1.0))
        val INFINITY: BoundingBox = BoundingBox(
            Pos3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
            Pos3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
        )
    }

    val minimum: Pos3D
    val maximum: Pos3D
    val center: Pos3D
    val dimensions: Pos3D

    init {
        if (minimum.isInfinite() && maximum.isInfinite()) {
            this.minimum = minimum
            this.maximum = maximum
            center = Pos3D(0.0, 0.0, 0.0)
            dimensions = Pos3D(0.0, 0.0, 0.0)
        } else {
            this.minimum = Pos3D(
                minimum.x.coerceAtMost(maximum.x),
                minimum.y.coerceAtMost(maximum.y),
                minimum.z.coerceAtMost(maximum.z)
            )
            this.maximum = Pos3D(
                minimum.x.coerceAtLeast(maximum.x),
                minimum.y.coerceAtLeast(maximum.y),
                minimum.z.coerceAtLeast(maximum.z)
            )
            center = (this.minimum + this.maximum) * 0.5
            dimensions = this.maximum - this.minimum
        }
    }

    /**
     * Constructs the new bounding box using the provided minimum and maximum coordinates.
     *
     * @param minimumX the minimum x.
     * @param minimumY the minimum y.
     * @param minimumZ the minimum z.
     * @param maximumX the maximum x.
     * @param maximumY the maximum y.
     * @param maximumZ the maximum z.
     */
    constructor(
        minimumX: Double,
        minimumY: Double,
        minimumZ: Double,
        maximumX: Double,
        maximumY: Double,
        maximumZ: Double
    ) : this(
        Pos3D(minimumX, minimumY, minimumZ), Pos3D(maximumX, maximumY, maximumZ)
    )

    /**
     * Gets the width of this bounding box.
     *
     * This is the equivalent of using: `BoundingBox.getDimensions().getX()`.
     *
     * @return the width of the bounding box.
     */
    val width: Double
        get() = dimensions.x

    /**
     * Gets the height of this bounding box.
     *
     * This is the equivalent of using: `BoundingBox.getDimensions().getY()`.
     *
     * @return the height of the bounding box.
     */
    val height: Double
        get() = dimensions.y

    /**
     * Gets the depth of this bounding box.
     *
     * This is the equivalent of using: `BoundingBox.getDimensions().getZ()`.
     *
     * @return the depth of the bounding box.
     */
    val depth: Double
        get() = dimensions.z

    /* BoundingBox Corners */
    val corner000: Pos3D
        get() = Pos3D(minimum.x, minimum.y, minimum.z)

    val corner001: Pos3D
        get() = Pos3D(minimum.x, minimum.y, maximum.z)

    val corner010: Pos3D
        get() = Pos3D(minimum.x, maximum.y, minimum.z)

    val corner011: Pos3D
        get() = Pos3D(minimum.x, maximum.y, maximum.z)

    val corner100: Pos3D
        get() = Pos3D(maximum.x, minimum.y, minimum.z)

    val corner101: Pos3D
        get() = Pos3D(maximum.x, minimum.y, maximum.z)

    val corner110: Pos3D
        get() = Pos3D(maximum.x, maximum.y, minimum.z)

    val corner111: Pos3D
        get() = Pos3D(maximum.x, maximum.y, maximum.z)

    val corners: Array<Pos3D>
        get() = arrayOf(
            corner000,
            corner001,
            corner010,
            corner011,
            corner100,
            corner101,
            corner110,
            corner111
        )

    /**
     * Adds the provided location [Pos3D].
     *
     * This is commonly used for locating unit bounding boxes.
     *
     * @param location the location to add.
     * @return a new BoundingBox containing the addition result.
     */
    fun add(location: Pos3D): BoundingBox {
        return BoundingBox(minimum.plus(location), maximum.plus(location))
    }

    /**
     * Adds the provided location coordinates.
     *
     * This is commonly used for locating unit bounding boxes.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     * @return a new BoundingBox containing the addition result.
     */
    fun add(x: Double, y: Double, z: Double): BoundingBox {
        return this.add(Pos3D(x, y, z))
    }

    /**
     * Subtract by the provided location [Pos3D].
     *
     * @param location the location to subtract.
     * @return a new BoundingBox containing the subtraction result.
     */
    fun subtract(location: Pos3D): BoundingBox {
        return BoundingBox(minimum.minus(location), maximum.minus(location))
    }

    /**
     * Subtract by the provided location coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     * @return a new BoundingBox containing the subtraction result.
     */
    fun subtract(x: Double, y: Double, z: Double): BoundingBox {
        return this.subtract(Pos3D(x, y, z))
    }

    /**
     * Multiply by the provided location [Pos3D].
     *
     * @param location the location to multiply.
     * @return a new BoundingBox containing the multiplication result.
     */
    fun multiply(location: Pos3D): BoundingBox {
        return BoundingBox(minimum.times(location), maximum.times(location))
    }

    /**
     * Multiply by the provided location coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     * @return a new BoundingBox containing the multiplication result.
     */
    fun multiply(x: Double, y: Double, z: Double): BoundingBox {
        return this.multiply(Pos3D(x, y, z))
    }

    /**
     * Divide by the provided location [Pos3D].
     *
     * @param location the location to divide.
     * @return a new BoundingBox containing the division result.
     */
    fun divide(location: Pos3D): BoundingBox {
        return BoundingBox(minimum.div(location), maximum.div(location))
    }

    /**
     * Divide by the provided location coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     * @return a new BoundingBox containing the division result.
     */
    fun divide(x: Double, y: Double, z: Double): BoundingBox {
        return this.divide(Pos3D(x, y, z))
    }

    /**
     * Extends this bounding box by the provided bounding box.
     *
     * @param aBounds the other bounding box.
     * @return a new BoundingBox containing the extension result.
     */
    fun extend(aBounds: BoundingBox): BoundingBox {
        return BoundingBox(
            Pos3D(
                minimum.x.coerceAtMost(aBounds.minimum.x),
                minimum.y.coerceAtMost(aBounds.minimum.y),
                minimum.z.coerceAtMost(aBounds.minimum.z)
            ), Pos3D(
                maximum.x.coerceAtLeast(aBounds.maximum.x),
                maximum.y.coerceAtLeast(aBounds.maximum.y),
                maximum.z.coerceAtLeast(aBounds.maximum.z)
            )
        )
    }

    /**
     * Extends this bounding box to incorporate the provided [Pos3D].
     *
     * @param point the point to incorporate.
     * @return a new BoundingBox containing the extension result.
     */
    fun extend(point: Pos3D): BoundingBox {
        return this.extend(point.x, point.y, point.z)
    }

    /**
     * Extends this bounding box to incorporate the provided coordinates.
     *
     * @param x the x-coordinate.
     * @param y the y-coordinate.
     * @param z the z-coordinate.
     * @return a new BoundingBox containing the extension result.
     */
    fun extend(x: Double, y: Double, z: Double): BoundingBox {
        return BoundingBox(
            Pos3D(
                minimum.x.coerceAtMost(x),
                minimum.y.coerceAtMost(y),
                minimum.z.coerceAtMost(z)
            ), Pos3D(
                maximum.x.coerceAtLeast(x),
                maximum.y.coerceAtLeast(y),
                maximum.z.coerceAtLeast(z)
            )
        )
    }

    /**
     * Extends this bounding box by the given sphere.
     *
     * @param center the sphere center.
     * @param radius the sphere radius.
     * @return a new BoundingBox containing the extension result.
     */
    fun extend(center: Pos3D, radius: Double): BoundingBox {
        return BoundingBox(
            Pos3D(
                minimum.x.coerceAtMost((center.x - radius)),
                minimum.y.coerceAtMost((center.y - radius)),
                minimum.z.coerceAtMost((center.z - radius))
            ), Pos3D(
                maximum.x.coerceAtLeast((center.x + radius)),
                maximum.y.coerceAtLeast((center.y + radius)),
                maximum.z.coerceAtLeast((center.z + radius))
            )
        )
    }

    /**
     * Gets whether the provided [BoundingBox] is intersecting this bounding box ( at least
     * one point in ).
     *
     * @param other the bounding box to check.
     * @return true if intersecting.
     */
    fun intersects(other: BoundingBox): Boolean {
        /* test using SAT (separating axis theorem) */
        val lx: Double = abs(center.x - other.center.x)
        val sumX: Double = (dimensions.x / 2.0f) + (other.dimensions.x / 2.0f)
        val ly: Double = abs(center.y - other.center.y)
        val sumY: Double = (dimensions.y / 2.0f) + (other.dimensions.y / 2.0f)
        val lz: Double = abs(center.z - other.center.z)
        val sumZ: Double = (dimensions.z / 2.0f) + (other.dimensions.z / 2.0f)
        return ((lx <= sumX) && (ly <= sumY) && (lz <= sumZ))
    }

    operator fun contains(Pos3D: Pos3D): Boolean {
        return ((minimum.x <= Pos3D.x
                ) && (maximum.x >= Pos3D.x
                ) && (minimum.y <= Pos3D.y
                ) && (maximum.y >= Pos3D.y
                ) && (minimum.z <= Pos3D.z
                ) && (maximum.z >= Pos3D.z))
    }

    operator fun contains(other: BoundingBox): Boolean {
        return (minimum.x <= other.minimum.x
                ) && (minimum.y <= other.minimum.y
                ) && (minimum.z <= other.minimum.z
                ) && (maximum.x >= other.maximum.x
                ) && (maximum.y >= other.maximum.y
                ) && (maximum.z >= other.maximum.z)
    }

    fun getBlocks(world: World): Set<Block> {
        val blocks: MutableSet<Block> = HashSet()
        val minimum = minimum.toBlockPos()
        val maximum = maximum.toBlockPos()
        for (x in minimum.x..maximum.x) {
            for (y in minimum.y..maximum.y) {
                for (z in minimum.z..maximum.z) {
                    blocks.add(world.getBlockAt(x, y, z))
                }
            }
        }
        return blocks
    }

    fun getOutline(skipDistance: Double): List<Pos3D> {
        val positions: MutableList<Pos3D> = ArrayList()
        var x: Double = minimum.x + skipDistance
        while (x < maximum.x) {
            positions.add(Pos3D(x, minimum.y, minimum.z))
            positions.add(Pos3D(x, maximum.z, minimum.z))
            positions.add(Pos3D(x, minimum.y, maximum.z))
            positions.add(Pos3D(x, maximum.y, maximum.z))
            x += skipDistance
        }
        var y: Double = minimum.y + skipDistance
        while (y < maximum.y) {
            positions.add(Pos3D(minimum.x, y, minimum.z))
            positions.add(Pos3D(maximum.x, y, minimum.z))
            positions.add(Pos3D(minimum.x, y, maximum.z))
            positions.add(Pos3D(maximum.x, y, maximum.z))
            y += skipDistance
        }
        var z: Double = minimum.z + skipDistance
        while (z < maximum.z) {
            positions.add(Pos3D(minimum.x, minimum.y, z))
            positions.add(Pos3D(maximum.x, minimum.y, z))
            positions.add(Pos3D(minimum.x, maximum.y, z))
            positions.add(Pos3D(maximum.x, maximum.y, z))
            z += skipDistance
        }
        positions.addAll(corners.toList())
        return positions
    }

    val isValid: Boolean
        get() {
            return (minimum.x <= maximum.x
                    ) && (minimum.y <= maximum.y
                    ) && (minimum.z <= maximum.z)
        }

    override fun toString(): String {
        return "Box [ " + minimum.toString().replace(",", "::") + " -> " + maximum.toString().replace(",", "::") + " ]"
    }

    override fun hashCode(): Int {
        val prime: Int = 31
        var result: Int = 1
        result = prime * result + center.hashCode()
        result = prime * result + dimensions.hashCode()
        result = prime * result + maximum.hashCode()
        result = prime * result + minimum.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is BoundingBox) {
            return false
        }
        val other: BoundingBox = other
        if (maximum != other.maximum || minimum != other.minimum) {
            return false
        }
        return (center == other.center) && (dimensions == other.dimensions)
    }

}