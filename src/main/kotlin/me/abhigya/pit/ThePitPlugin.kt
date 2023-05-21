package me.abhigya.pit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jonahseguin.drink.CommandService
import com.jonahseguin.drink.command.DrinkCommandService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.abhigya.pit.database.Database
import me.abhigya.pit.database.DatabaseType
import me.abhigya.pit.database.sql.h2.H2
import me.abhigya.pit.database.sql.mysql.MySQL
import me.abhigya.pit.database.sql.postgresql.PostGreSQL
import me.abhigya.pit.util.Platform
import net.kyori.adventure.platform.AudienceProvider
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
        KTP.setConfiguration(Configuration.forDevelopment())
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

        val db = when (config.database.databaseType) {
            DatabaseType.H2 -> H2(File(this.dataFolder, "database.db"))
            DatabaseType.MYSQL -> MySQL(
                config.database.host,
                config.database.port,
                config.database.database,
                config.database.username,
                config.database.password,
                config.database.params
            )
            DatabaseType.PostGreSQL -> PostGreSQL(
                config.database.host,
                config.database.port,
                config.database.database,
                config.database.username,
                config.database.password,
                config.database.params
            )
        }

        runCatching {
            db.connect()
        }.onFailure {
            scope.getInstance<Logger>().log(Level.SEVERE, "Failed to connect to database", it)
            this.server.pluginManager.disablePlugin(this)
            return
        }

        scope.installModules(
            module {
                bind<Database>().toInstance(db)
                bind<me.abhigya.pit.configuration.Configuration>().toInstance(config)
            }
        )
    }

    override fun onDisable() {
        KTP.closeScope(this::class.java)
    }

    fun runCompatibilitySetup() {
        if (Platform.CURRENT == Platform.PAPER) {
            val paperCfg = File("paper.yml")
            if (paperCfg.exists()) {
                YamlConfigurationLoader.builder()
                    .file(paperCfg)
                    .build().let {
                        val cfg = it.load()
                        cfg.node("warnWhenSettingExcessiveVelocity").set(false)
                        it.save(cfg)
                    }
            }
        }
    }
}