package me.abhigya.pit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import me.abhigya.pit.configuration.Configs
import me.abhigya.pit.configuration.ConfigsImpl
import me.abhigya.pit.util.Platform
import net.kyori.adventure.platform.AudienceProvider
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import toothpick.Scope
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance
import java.io.File
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger

class ThePitPlugin : JavaPlugin(), CoroutineScope by CoroutineScope(
    SupervisorJob() + CoroutineName("Pit")
) {

    companion object {
        fun getPlugin(): ThePitPlugin {
            return KTP.openRootScope().getInstance()
        }
    }

    val scope: Scope

    init {
        KTP.setConfiguration(getConfiguration())
        KTP.openRootScope()
            .installModules(
                module {
                    bind<ThePitPlugin>().toInstance(this@ThePitPlugin)
                }
            )
        scope = KTP.openScope(this) {
            it.installModules(
                module {
                    bind<JavaPlugin>().toInstance(this@ThePitPlugin)
                    bind<Plugin>().toInstance(this@ThePitPlugin)
                    bind<Logger>().toInstance(this@ThePitPlugin.logger)
                    bind<ClassLoader>().withName("pluginClassLoader").toProviderInstance { this@ThePitPlugin.classLoader }
                    bind<CoroutineScope>().toInstance(this@ThePitPlugin)
                    bind<MiniMessage>().toProviderInstance {
                        MiniMessage.builder().build()
                    }.providesSingleton().providesReleasable()
                    bind<AudienceProvider>().toProviderInstance {
                        BukkitAudiences.create(this@ThePitPlugin)
                    }.providesSingleton().providesReleasable()
                    bind<Gson>().toProviderInstance {
                        GsonBuilder().disableHtmlEscaping().serializeNulls().create()
                    }.providesSingleton().providesReleasable()
                    bind<Path>().withName("dataFolder").toInstance(this@ThePitPlugin.dataFolder.toPath())
                    bind<Configs>().toClass(ConfigsImpl::class).singleton()
                }
            ).supportScopeAnnotation(PitPluginScope::class.java)
        }
    }

    override fun onEnable() {
        logger.log(Level.INFO, "Running compatibility setup...")
        runCatching {
            if (runCompatibilitySetup()) {
                logger.log(Level.INFO, "Compatibility setup success. Restarting server...")
                this.server.shutdown()
                return
            } else {
                logger.log(Level.INFO, "Compatibility up to date!")
            }
        }.onFailure {
            logger.log(Level.SEVERE, "Compatibility setup failed!", it)
            this.server.pluginManager.disablePlugin(this)
            return
        }

        val config = scope.getInstance<Configs>() as ConfigsImpl
        runBlocking {
            val result = config.reloadConfigs()

            if (!result.isSuccess()) {
                logger.log(Level.SEVERE, "Failed to load configs!")
            }
        }

//        val db = when (config.database.vendor) {
//            Vendor.H2 -> H2(File(this.dataFolder, "database.db"))
//            Vendor.MYSQL -> MariaDB(
//                config.database.host,
//                config.database.port,
//                config.database.database,
//                config.database.username,
//                config.database.password,
//                config.database.params
//            )
//            Vendor.PostGreSQL -> PostGreSQL(
//                config.database.host,
//                config.database.port,
//                config.database.database,
//                config.database.username,
//                config.database.password,
//                config.database.params
//            )
//        }
//
//        runCatching {
//            db.connect()
//        }.onFailure {
//            scope.getInstance<Logger>().log(Level.SEVERE, "Failed to connect to database", it)
//            this.server.pluginManager.disablePlugin(this)
//            return
//        }

//        scope.installModules(
//            module {
//                bind<Database>().toInstance(db)
//                bind<SQLDatabase>().toInstance(db)
//            }
//        )
    }

    override fun onDisable() {
        KTP.closeScope(this)
    }

    private fun runCompatibilitySetup(): Boolean {
        var isDirty = false

        fun patchConfig(file: File, vararg patches: Pair<(Configuration) -> Boolean, (Configuration) -> Unit>) {
            val config = YamlConfiguration.loadConfiguration(file)
            var localDirty = false
            for (patch in patches) {
                if (patch.first(config)) {
                    patch.second(config)
                    isDirty = true
                    localDirty = true
                }
            }

            if (localDirty) config.save(file)
        }

        if (Platform.CURRENT == Platform.PAPER) {
            File("paper.yml").runCatching {
                patchConfig(this, Pair({
                    it.getBoolean("warnWhenSettingExcessiveVelocity", true)
                }, {
                    it.set("warnWhenSettingExcessiveVelocity", false)
                }))
            }.onFailure {
                throw CompatibilitySetupException("Failed to patch paper.yml", it)
            }
        }

        if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
            File("plugins/ViaVersion/config.yml").runCatching {
                patchConfig(this, Pair({
                    !it.getBoolean("use-1_15-instant-respawn", false)
                }, {
                    it.set("use-1_15-instant-respawn", true)
                }))
            }.onFailure {
                throw CompatibilitySetupException("Failed to patch ViaVersion config.yml", it)
            }
        }

        return isDirty
    }

}

class CompatibilitySetupException(message: String, cause: Throwable? = null) : Exception(message, cause)