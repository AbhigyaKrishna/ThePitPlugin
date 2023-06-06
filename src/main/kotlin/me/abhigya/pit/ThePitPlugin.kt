package me.abhigya.pit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jonahseguin.drink.CommandService
import com.jonahseguin.drink.command.DrinkCommandService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.abhigya.pit.database.Database
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.sql.SQLDatabase
import me.abhigya.pit.database.sql.mariadb.MariaDB
import me.abhigya.pit.database.sql.postgresql.PostGreSQL
import me.abhigya.pit.util.Platform
import net.kyori.adventure.platform.AudienceProvider
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import toothpick.Scope
import toothpick.configuration.Configuration
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

@kr.entree.spigradle.Plugin
class ThePitPlugin : JavaPlugin(), CoroutineScope by CoroutineScope(
    SupervisorJob() + CoroutineName("Pit")
) {

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

        KTP.setConfiguration(if (System.getProperty("environment") == "development" ||
            System.getProperty("toothpick.configuration") == "development") {
            Configuration.forDevelopment()
        } else {
            Configuration.forProduction()
        })

        val scope = KTP.openScope(this::class.java) {
            it.installModules(
                module {
                    bind<JavaPlugin>().toInstance(this@ThePitPlugin)
                    bind<Plugin>().toInstance(this@ThePitPlugin)
                    bind<Logger>().toInstance(this@ThePitPlugin.logger)
                    bind<CoroutineScope>().toInstance(this@ThePitPlugin)
                    bind<MiniMessage>().toInstance(MiniMessage.builder().build())
                    bind<AudienceProvider>().toInstance(BukkitAudiences.create(this@ThePitPlugin))
                    bind<Gson>().toInstance(GsonBuilder().disableHtmlEscaping().serializeNulls().create())
                    bind<CommandService>().toInstance(DrinkCommandService(this@ThePitPlugin))
                },
                module {
                    bind<Scope>().toInstance(it)
                }
            )
        }

        val config = scope.getInstance<me.abhigya.pit.configuration.Configuration>()
        config.saveDefaults()

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

        scope.installModules(
            module {
                bind<Database>().toInstance(db)
                bind<SQLDatabase>().toInstance(db)
            }
        )
    }

    override fun onDisable() {
        KTP.closeScope(this::class.java)
    }

    private fun runCompatibilitySetup(): Boolean {
        var isDirty = false

        fun patchConfig(file: File, vararg patches: Triple<String, (ScopedConfigurationNode<*>) -> Boolean, Any>) {
            YamlConfigurationLoader.builder()
                .file(file)
                .build().run {
                    val cfg = this.load()
                    var localDirty = false
                    for (patch in patches) {
                        val node = cfg.node(patch.first.split("."))
                        if (patch.second(node)) {
                            node.set(patch.third)
                            isDirty = true
                            localDirty = true
                        }
                    }

                    if (localDirty) this.save(cfg)
                }
        }

        if (Platform.CURRENT == Platform.PAPER) {
            File("paper.yml").runCatching {
                patchConfig(this, Triple("warnWhenSettingExcessiveVelocity", { it.getBoolean(true) }, false))
            }.onFailure {
                throw CompatibilitySetupException("Failed to patch paper.yml", it)
            }
        }

        if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
            File("plugins/ViaVersion/config.yml").runCatching {
                patchConfig(this, Triple("use-1_15-instant-respawn", { !it.boolean }, true))
            }.onFailure {
                throw CompatibilitySetupException("Failed to patch ViaVersion config.yml", it)
            }
        }

        return isDirty
    }

}

class CompatibilitySetupException(message: String, cause: Throwable? = null) : Exception(message, cause)