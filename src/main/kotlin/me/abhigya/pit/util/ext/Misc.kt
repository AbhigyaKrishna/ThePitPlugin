package me.abhigya.pit.util.ext

import org.bukkit.GameRule
import org.bukkit.World
import java.util.logging.Logger

inline fun <reified T> logger(): Logger = Logger.getLogger(T::class.java.name)

class GameRuleValue(val rule: String, val value: Any) {
    fun key(): GameRule<*> {
        return GameRule.getByName(rule) ?: throw IllegalArgumentException("Invalid game rule: $rule")
    }
}

fun <T> GameRule<T>.value(value: Any): GameRuleValue = GameRuleValue(this.name, value)

fun <T : Any> World.setGameRule(value: GameRuleValue) {
    runCatching {
        val rule = value.key()
        val type = rule.type
        if (type == value.value.javaClass) {
            this.setGameRule(rule as GameRule<T>, value.value as T)
        }
    }
}