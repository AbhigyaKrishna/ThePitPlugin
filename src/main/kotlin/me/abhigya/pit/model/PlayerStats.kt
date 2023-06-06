package me.abhigya.pit.model

class PlayerStats {

    companion object StatKeys {
        val BOW_DAMAGE_TAKEN = DoubleStatKey("bow_damage_taken")
        val DAMAGE_TAKEN = DoubleStatKey("damage_taken")
        val DEATHS = IntStatKey("deaths")
        val MELEE_DAMAGE_TAKEN = DoubleStatKey("melee_damage_taken")
        val BLOCKS_BROKEN = IntStatKey("blocks_broken")
        val BLOCKS_PLACED = IntStatKey("blocks_placed")
        val CHAT_MESSAGES = IntStatKey("chat_messages")
        val FISHING_RODS_LAUNCHED = IntStatKey("fishing_rods_launched")
        val GOLDEN_APPLES_EATEN = IntStatKey("golden_apples_eaten")
        val JUMPS_INTO_PIT = IntStatKey("jumps_into_pit")
        val LAVA_BUCKETS_EMPTIED = IntStatKey("lava_buckets_emptied")
        val LEFT_CLICKS = IntStatKey("left_clicks")
        val ARROW_HITS = IntStatKey("arrow_hits")
        val ARROWS_SHOTS = IntStatKey("arrows_shots")
        val ASSISTS = IntStatKey("assists")
        val BOW_DAMAGE_DEALT = DoubleStatKey("bow_damage_dealt")
        val DIAMOND_ITEMS_PURCHASED = IntStatKey("diamond_items_purchased")
        val LAUNCHES = IntStatKey("launches")
        val DAMAGE_DEALT = DoubleStatKey("damage_dealt")
        val HIGHEST_STREAK = IntStatKey("highest_streak")
        val KILLS = IntStatKey("kills")
        val MELEE_DAMAGE_DEALT = DoubleStatKey("melee_damage_dealt")
        val SWORD_HITS = IntStatKey("sword_hits")
        val GOLD_EARNED = IntStatKey("gold_earned")
        val GOLDEN_HEADS_EATEN = IntStatKey("golden_heads_eaten")

        val VALUES = arrayOf(
            BOW_DAMAGE_DEALT,
            BOW_DAMAGE_TAKEN,
            DAMAGE_DEALT,
            DAMAGE_TAKEN,
            DEATHS,
            MELEE_DAMAGE_DEALT,
            MELEE_DAMAGE_TAKEN,
            BLOCKS_BROKEN,
            BLOCKS_PLACED,
            CHAT_MESSAGES,
            FISHING_RODS_LAUNCHED,
            GOLDEN_APPLES_EATEN,
            JUMPS_INTO_PIT,
            LAVA_BUCKETS_EMPTIED,
            LEFT_CLICKS,
            ARROW_HITS,
            ARROWS_SHOTS,
            ASSISTS,
            DIAMOND_ITEMS_PURCHASED,
            LAUNCHES,
            HIGHEST_STREAK,
            KILLS,
            SWORD_HITS,
            GOLD_EARNED,
            GOLDEN_HEADS_EATEN
        )
    }

    private val stats: MutableMap<StatKey<*>, Number> = mutableMapOf()

    operator fun <T : Number> get(key: StatKey<T>): T? {
        return stats[key] as? T
    }

    operator fun <T : Number> set(key: StatKey<T>, value: T) {
        stats[key] = value
    }

}

abstract class StatKey<out T : Number> internal constructor(val name: String, val type: Class<@UnsafeVariance T>) {
    fun key(): String = name.uppercase()

    abstract fun zeroValue(): T
}

class DoubleStatKey internal constructor(name: String) : StatKey<Double>(name, Double::class.java) {
    override fun zeroValue(): Double = 0.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleStatKey) return false
        return other.name == name
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "DoubleStatKey($name)"
    }

}

class IntStatKey internal constructor(name: String) : StatKey<Int>(name, Int::class.java) {
    override fun zeroValue(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntStatKey) return false
        return other.name == name
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "IntStatKey($name)"
    }

}