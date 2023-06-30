package me.abhigya.pit.addon

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import me.abhigya.pit.ThePitPlugin
import me.abhigya.pit.util.ext.logger
import org.bukkit.event.HandlerList
import org.bukkit.plugin.InvalidPluginException
import org.bukkit.plugin.UnknownDependencyException
import space.arim.dazzleconf.ConfigurationOptions
import space.arim.dazzleconf.error.InvalidConfigException
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory
import toothpick.InjectConstructor
import toothpick.Scope
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarFile
import java.util.logging.Level
import javax.inject.Named
import javax.inject.Singleton
import kotlin.io.path.listDirectoryEntries
import kotlin.system.measureTimeMillis

interface AddonManager {

    fun getAddon(name: String): Addon?

    fun getAddons(): Collection<Addon>

    fun loadAddons(path: Path): Array<Addon>

    @Throws(InvalidPluginException::class)
    fun loadAddon(path: Path): Addon

    fun enableAddon(addon: Addon)

    fun disableAddon(addon: Addon)

}

@Singleton
@InjectConstructor
class SimpleAddonManager(
    @Named("dataFolder") private val dataFolder: Path,
    @Named("pluginClassLoader") private val parentClassLoader: ClassLoader,
    private val plugin: ThePitPlugin,
    private val scope: Scope
) : AddonManager {

    private val addons: MutableMap<String, Addon> = ConcurrentHashMap()
    private val loaders: MutableList<AddonClassLoader> = CopyOnWriteArrayList()
    private val dependencyGraph: MutableGraph<String> = GraphBuilder.directed().build()
    private val logger = logger<AddonManager>()
    private val addonDescriptionLoader = SnakeYamlConfigurationFactory.create(AddonDescription::class.java, ConfigurationOptions.defaults())

    override fun getAddon(name: String): Addon? {
        return addons[name]
    }

    override fun getAddons(): Collection<Addon> {
        return addons.values
    }

    @Synchronized
    override fun enableAddon(addon: Addon) {
        if (addon.isEnabled) {
            return
        }

        logger.log(Level.INFO, "Enabling ${addon.name} v${addon.description.version()} by ${addon.description.authors()}")
        try {
            val time = measureTimeMillis {
                addon.initiate()
                addon.isEnabled = true
                for (listener in addon.listeners) {
                    plugin.server.pluginManager.registerEvents(listener, plugin)
                }
            }

            logger.log(Level.INFO, "Enabled ${addon.name} in ${time}ms!")
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Error occurred while enabling addon ${addon.name}", e)
        }
    }

    @Synchronized
    override fun disableAddon(addon: Addon) {
        if (!addon.isEnabled) {
            return
        }

        logger.log(Level.INFO, "Disabling ${addon.name}")
        try {
            addon.disable()
            addon.isEnabled = false
            for (listener in addon.listeners) {
                HandlerList.unregisterAll(listener)
            }

            logger.log(Level.INFO, "Disabled ${addon.name}!")
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Error occurred while disabling addon ${addon.name}", e)
        }
    }

    override fun loadAddons(directory: Path): Array<Addon> {
        val result = ArrayList<Addon>()
        val addons = HashMap<String, File>()
        val loadedAddons = HashSet<String>()
        val dependencies = HashMap<String, MutableList<String>>() // addon -> dependencies
        val softDependencies = HashMap<String, MutableList<String>>() // addon -> soft dependencies

        for (filePath in directory.listDirectoryEntries("\\.jar$")) {
            val file = filePath.toFile()

            val description = try {
                val description = getDescription(file)
                val name = description.name()

                if (name.equals("bukkit", ignoreCase = true) ||
                    name.equals("minecraft", ignoreCase = true) ||
                    name.equals("mojang", ignoreCase = true)
                ) {
                    logger.log(
                        Level.SEVERE,
                        "Could not load '${file.path}' in folder '$directory': Restricted Name"
                    )
                    continue
                }

                if (name.contains(" ")) {
                    logger.log(
                        Level.SEVERE,
                        "Could not load '${file.path}' in folder '$directory': uses the space-character (0x20) in its name"
                    )
                    continue
                }

                description
            } catch (e: InvalidAddonException) {
                logger.log(
                    Level.SEVERE,
                    "Could not load '${file.path}' in folder '$directory': " + e.message
                )
                continue
            }

            val name = description.name()
            val replaced = addons.put(name, file)
            if (replaced != null) {
                logger.log(
                    Level.SEVERE,
                    "Ambiguous addon name $name for files ${file.path} and ${replaced.path}"
                )
            }

            val softDepend = description.softDepends()
            if (softDepend.isNotEmpty()) {
                softDependencies.compute(name) { _, list ->
                    if (list == null) {
                        softDepend.toMutableList()
                    } else {
                        list.addAll(softDepend)
                        list
                    }
                }

                for (s in softDepend) {
                    dependencyGraph.putEdge(name, s)
                }
            }

            val depend = description.depends()
            if (depend.isNotEmpty()) {
                dependencies[name] = depend.toMutableList()

                for (s in depend) {
                    dependencyGraph.putEdge(name, s)
                }
            }
        }

        while (addons.isNotEmpty()) {
            var missingDependency = true
            var pluginIterator = addons.entries.iterator()
            while (pluginIterator.hasNext()) {
                val (addon, value) = pluginIterator.next()
                if (dependencies.containsKey(addon)) {
                    val dependencyIterator = dependencies[addon]!!.iterator()
                    while (dependencyIterator.hasNext()) {
                        val dependency = dependencyIterator.next()

                        // Dependency loaded
                        if (loadedAddons.contains(dependency)) {
                            dependencyIterator.remove()

                            // We have a dependency not found
                        } else if (!addons.containsKey(dependency)) {
                            missingDependency = false
                            pluginIterator.remove()
                            softDependencies.remove(addon)
                            dependencies.remove(addon)
                            logger.log(
                                Level.SEVERE,
                                "Could not load '${value.path}' in folder '$directory'",
                                UnknownDependencyException("Unknown dependency $dependency. Please download and install $dependency to run this plugin.")
                            )
                            break
                        }
                    }

                    if (dependencies.containsKey(addon) && dependencies[addon]!!.isEmpty()) {
                        dependencies.remove(addon)
                    }
                }

                if (softDependencies.containsKey(addon)) {
                    val softDependencyIterator = softDependencies[addon]!!.iterator()
                    while (softDependencyIterator.hasNext()) {
                        val softDependency = softDependencyIterator.next()

                        // Soft depend is no longer around
                        if (!addons.containsKey(softDependency)) {
                            softDependencyIterator.remove()
                        }
                    }
                    if (softDependencies[addon]!!.isEmpty()) {
                        softDependencies.remove(addon)
                    }
                }
                if (!(dependencies.containsKey(addon) || softDependencies.containsKey(addon)) && addons.containsKey(addon)) {
                    // We're clear to load, no more soft or hard dependencies left
                    pluginIterator.remove()
                    missingDependency = false
                    try {
                        val loadedPlugin = loadAddon(value.toPath())
                        result.add(loadedPlugin)
                        loadedAddons.add(loadedPlugin.name)
                        continue
                    } catch (ex: InvalidPluginException) {
                        logger.log(
                            Level.SEVERE,
                            "Could not load '${value.path}' in folder '$directory'",
                            ex
                        )
                    }
                }
            }
            if (missingDependency) {
                // We now iterate over plugins until something loads
                // This loop will ignore soft dependencies
                pluginIterator = addons.entries.iterator()
                while (pluginIterator.hasNext()) {
                    val (plugin, file) = pluginIterator.next()
                    if (!dependencies.containsKey(plugin)) {
                        softDependencies.remove(plugin)
                        missingDependency = false
                        pluginIterator.remove()
                        try {
                            val loadedPlugin = loadAddon(file.toPath())
                            result.add(loadedPlugin)
                            loadedAddons.add(loadedPlugin.name)
                            break
                        } catch (ex: InvalidPluginException) {
                            logger.log(
                                Level.SEVERE,
                                "Could not load '${file.path}' in folder '$directory'",
                                ex
                            )
                        }
                    }
                }

                // We have no plugins left without a depend
                if (missingDependency) {
                    softDependencies.clear()
                    dependencies.clear()
                    val failedPluginIterator: MutableIterator<File> = addons.values.iterator()
                    while (failedPluginIterator.hasNext()) {
                        val file = failedPluginIterator.next()
                        failedPluginIterator.remove()
                        logger.log(
                            Level.SEVERE,
                            "Could not load '${file.path}' in folder '$directory': circular dependency detected"
                        )
                    }
                }
            }
        }

        return result.toTypedArray()
    }

    @Throws(InvalidAddonException::class)
    override fun loadAddon(path: Path): Addon {
        val file = path.toFile()
        if (!file.exists()) {
            throw IllegalAccessException("Addon file does not exist!")
        }

        val description = getDescription(file)
        val result = loadAddon0(file, description)

        addons[result.description.name()] = result
        return result
    }

    @Throws(InvalidAddonException::class)
    private fun loadAddon0(file: File, description: AddonDescription): Addon {
        val addonFolder = dataFolder.resolve("addons/${description.name()}")

        for (depend in description.depends()) {
            if (!addons.containsKey(depend)) {
                throw IllegalAccessException("Addon ${description.name()} depends on $depend, which is not loaded!")
            }
        }

        val addonScope = scope.openSubScope(description.name())
            .installModules(
                module {
                    bind<Path>().withName("dataFolder").toInstance(addonFolder)
                    bind<File>().withName("addonFile").toInstance(file)
                    bind<AddonDescription>().toInstance(description)
                    bind<AddonManager>().toInstance(this@SimpleAddonManager)
                }
            )

        val classLoader = AddonClassLoader(
            parentClassLoader,
            description,
            addonScope,
            file
        )

        addonScope.installModules(
            module {
                bind<ClassLoader>().withName("addonClassLoader").toInstance(classLoader)
            }
        )

        loaders.add(classLoader)
        return classLoader.addon
    }

    private fun getDescription(file: File): AddonDescription {
        try {
            JarFile(file).use {
                val entry = it.getJarEntry("addon.yml") ?: throw InvalidAddonException("Addon file does not contain addon.yml!")

                it.getInputStream(entry).use { stream ->
                    return addonDescriptionLoader.load(stream)
                }
            }
        } catch (e: IOException) {
            throw InvalidAddonException(e)
        } catch (e: InvalidConfigException) {
            throw InvalidAddonException(e)
        }
    }

}