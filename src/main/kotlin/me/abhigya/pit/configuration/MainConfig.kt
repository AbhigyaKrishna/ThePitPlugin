package me.abhigya.pit.configuration

import me.abhigya.pit.util.ext.GameRuleValue
import space.arim.dazzleconf.annote.ConfComments
import space.arim.dazzleconf.annote.ConfDefault
import space.arim.dazzleconf.annote.ConfKey

interface MainConfig {

    @ConfKey("game-rules")
    @ConfComments("Game rules to be applied to the world")
    @ConfDefault.DefaultStrings(
        "announceAdvancements false",
        "doDaylightCycle false",
        "doFireTick false",
        "doMobSpawning false",
        "keepInventory true",
        "mobGriefing false",
        "doPatrolSpawning false"
    )
    fun gameRules(): List<GameRuleValue>

//    @ConfKey("default-inventory")
//    fun defaultInventory(): DefaultInventoryConfig
//
//    interface DefaultInventoryConfig {
//
//        fun helmet(): String
//
//        fun chestplate(): String
//
//        fun leggings(): String
//
//        fun boots(): String
//
//        fun offHand(): String
//
//        fun items(): Map<Int, String>
//
//    }

}