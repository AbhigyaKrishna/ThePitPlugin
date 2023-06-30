package me.abhigya.pit.addon

import org.bukkit.Server
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import toothpick.Scope
import toothpick.ktp.extension.getInstance
import java.nio.file.Path
import java.util.logging.Logger

abstract class Addon(
    val scope: Scope
) {

    internal var isEnabled: Boolean = false

    val classLoader: ClassLoader get() = scope.getInstance<ClassLoader>("addonClassLoader")

    val dataFolder: Path get() = scope.getInstance<Path>("dataFolder")

    val logger: Logger = Logger.getLogger(javaClass.simpleName)

    val server: Server get() = scope.getInstance<JavaPlugin>().server

    val description: AddonDescription = scope.getInstance()

    val name get() = description.name()

    open fun initiate() {

    }

    open val listeners: Set<Listener> = emptySet()

    open fun disable() {

    }

}