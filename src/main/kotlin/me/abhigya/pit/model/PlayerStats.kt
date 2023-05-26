package me.abhigya.pit.model

class PlayerStats {

    companion object StatKeys {
        val BOW_DAMAGE_TAKEN = StatKey("bow_damage_taken")
        val DAMAGE_TAKEN = StatKey("damage_taken")
        val DEATHS = StatKey("deaths")
        val MELEE_DAMAGE_TAKEN = StatKey("melee_damage_taken")
        val BLOCKS_BROKEN = StatKey("blocks_broken")
        val BLOCKS_PLACED = StatKey("blocks_placed")
        val CHAT_MESSAGES = StatKey("chat_messages")
        val FISHING_RODS_LAUNCHED = StatKey("fishing_rods_launched")
        val GOLDEN_APPLES_EATEN = StatKey("golden_apples_eaten")
        val JUMPS_INTO_PIT = StatKey("jumps_into_pit")
        val LAVA_BUCKETS_EMPTIED = StatKey("lava_buckets_emptied")
        val LEFT_CLICKS = StatKey("left_clicks")
        val ARROW_HITS = StatKey("arrow_hits")
        val ARROWS_SHOT = StatKey("arrows_shot")
        val ASSISTS = StatKey("assists")
        val BOW_DAMAGE_DEALT = StatKey("bow_damage_dealt")
        val DIAMOND_ITEMS_PURCHASED = StatKey("diamond_items_purchased")
        val LAUNCHES = StatKey("launches")
        val DAMAGE_DEALT = StatKey("damage_dealt")
        val HIGHEST_STREAK = StatKey("highest_streak")
        val KILLS = StatKey("kills")
        val MELEE_DAMAGE_DEALT = StatKey("melee_damage_dealt")
        val SWORD_HITS = StatKey("sword_hits")
        val GOLD_EARNED = StatKey("gold_earned")
        val GOLDEN_HEADS_EATEN = StatKey("golden_heads_eaten")
    }

    private val stats: MutableMap<StatKey, Number> = mutableMapOf()

    operator fun <T : Number> get(key: StatKey): T? {
        return stats[key] as? T
    }

    operator fun <T : Number> set(key: StatKey, value: T) {
        stats[key] = value
    }

}

data class StatKey internal constructor(val name: String) {
    fun key(): String = name.uppercase()
}