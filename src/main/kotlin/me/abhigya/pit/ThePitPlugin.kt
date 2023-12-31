package me.abhigya.pit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import me.abhigya.pit.addon.AddonManager
import me.abhigya.pit.addon.SimpleAddonManager
import me.abhigya.pit.configuration.Configs
import me.abhigya.pit.configuration.ConfigsImpl
import me.abhigya.pit.database.Database
import me.abhigya.pit.database.DatabaseSettingsValidator
import me.abhigya.pit.database.FlywayMigration
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.sql.SQLDatabase
import me.abhigya.pit.database.sql.hsqldb.HSQLDB
import me.abhigya.pit.database.sql.mariadb.MariaDB
import me.abhigya.pit.database.sql.postgresql.PostGreSQL
import me.abhigya.pit.util.Platform
import me.abhigya.pit.util.ext.BukkitCoroutineDispatcher
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
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.system.measureTimeMillis

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
                    bind<Logger>().toProviderInstance { this@ThePitPlugin.logger }
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
                    bind<Path>().withName("dataFolder").toProviderInstance { this@ThePitPlugin.dataFolder.toPath() }
                    bind<Configs>().toClass(ConfigsImpl::class).singleton()
                    bind<AddonManager>().toClass(SimpleAddonManager::class).singleton()
                }
            ).supportScopeAnnotation(PitPluginScope::class.java)
        }
        scope.inject(BukkitCoroutineDispatcher)
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

        // Config
        val config = scope.getInstance<Configs>() as ConfigsImpl
        runBlocking {
            measureTimeMillis {
                val result = config.reloadConfigs()

                if (!result.isSuccess()) {
                    logger.log(Level.SEVERE, "Failed to load configs!")
                }
            }.run {
                logger.log(Level.INFO, "Loaded configs in ${this}ms!")
            }
        }

        // Database
        measureTimeMillis {
            val validator = DatabaseSettingsValidator(config.databaseConfig, logger)
            validator.validate()

            val database = when (validator.effectiveVendor) {
                Vendor.HSQLDB -> HSQLDB(dataFolder.toPath().resolve("database.db"), config.databaseConfig)
                Vendor.MYSQL -> MariaDB(Vendor.MYSQL, config.databaseConfig)
                Vendor.MARIADB -> MariaDB(Vendor.MARIADB, config.databaseConfig)
                Vendor.POSTGRESQL -> PostGreSQL(config.databaseConfig)
            }

            val success = runBlocking {
                try {
                    database.connect()
                    return@runBlocking true
                } catch (e: SQLException) {
                    logger.log(Level.SEVERE, "Failed to connect to database! The plugin won't run without connection to a database!", e)
                    server.pluginManager.disablePlugin(this@ThePitPlugin)
                    return@runBlocking false
                }
            }

            if (!success) {
                return
            }

            scope.installModules(
                module {
                    bind<Database>().toInstance(database)
                    bind<SQLDatabase>().toInstance(database)
                }
            )

            val migration = FlywayMigration(validator.effectiveVendor, database.dataSource!!)
            try {
                migration.migrate()
            } catch (e: SQLException) {
                logger.log(Level.SEVERE, "Unable to migrate your database. Please create a backup of your database "
                        + "and promptly report this issue.", e)
                server.pluginManager.disablePlugin(this@ThePitPlugin)
                return
            }
        }.run {
            logger.log(Level.INFO, "Connected to database in ${this}ms!")
        }

        // Addons
        val addonPath = dataFolder.toPath().resolve("addon")
        if (!addonPath.isDirectory())
            addonPath.createDirectories()

        val addonManager = scope.getInstance<AddonManager>()
        val loadedAddons = addonManager.loadAddons(addonPath)

        for (addon in loadedAddons) {
            addonManager.enableAddon(addon)
        }
    }

    override fun onDisable() {
        val addonManager = scope.getInstance<AddonManager>()
        for (addon in addonManager.getAddons()) {
            addonManager.disableAddon(addon)
        }

        runBlocking {
            val database = scope.getInstance<Database>()
            database.disconnect()
        }

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