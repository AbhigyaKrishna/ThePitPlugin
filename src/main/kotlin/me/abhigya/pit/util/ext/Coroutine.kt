package me.abhigya.pit.util.ext

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import toothpick.ktp.delegate.inject
import kotlin.coroutines.CoroutineContext

/**
 * A dispatcher that runs tasks on the bukkit main thread.
 */
object BukkitCoroutineDispatcher : CoroutineDispatcher() {

    internal val plugin: Plugin by inject()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Bukkit.getScheduler().runTask(plugin, block)
        }
    }

}

val Dispatchers.Bukkit: CoroutineDispatcher get() = BukkitCoroutineDispatcher