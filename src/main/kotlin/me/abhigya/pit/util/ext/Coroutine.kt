package me.abhigya.pit.util.ext

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.abhigya.pit.util.injectMembers
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * A dispatcher that runs tasks on the bukkit main thread.
 */
@SuppressWarnings("Injectable")
object BukkitCoroutineDispatcher : CoroutineDispatcher() {

    @Inject
    internal lateinit var plugin: Plugin

    init {
        injectMembers()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Bukkit.getScheduler().runTask(plugin, block)
        }
    }

}

val Dispatchers.Bukkit: CoroutineDispatcher get() = BukkitCoroutineDispatcher