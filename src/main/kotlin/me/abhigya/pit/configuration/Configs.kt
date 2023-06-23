package me.abhigya.pit.configuration

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.abhigya.pit.PitPluginScope
import org.yaml.snakeyaml.Yaml
import space.arim.dazzleconf.AuxiliaryKeys
import space.arim.dazzleconf.ConfigurationOptions
import space.arim.dazzleconf.error.ConfigFormatSyntaxException
import space.arim.dazzleconf.error.InvalidConfigException
import space.arim.dazzleconf.ext.snakeyaml.CommentMode
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions
import toothpick.InjectConstructor
import toothpick.Scope
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.HashSet
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.io.path.exists
import kotlin.reflect.KClass

interface Configs {

    val mainConfig: MainConfig

    val databaseConfig: DataBaseSettingsConfig

    val arenaConfigs: List<ArenaDataConfig>

}

@Singleton
@PitPluginScope
@InjectConstructor
class ConfigsImpl(
    private val scope: Scope,
    @Named("dataFolder") private val dataFolder: Path
) : Configs {

    private val configConfigProvider = ConfigProvider(MainConfig::class)
    private val databaseConfigProvider = ConfigProvider(DataBaseSettingsConfig::class)
    private val arenaConfigProvider = HashSet<ConfigProvider<ArenaDataConfig>>()

    init {
        scope.inject(configConfigProvider)
        scope.inject(databaseConfigProvider)
    }

    override val mainConfig: MainConfig = configConfigProvider.config

    override val databaseConfig: DataBaseSettingsConfig = databaseConfigProvider.config

    override val arenaConfigs: List<ArenaDataConfig> = arenaConfigProvider.map { it.config }

    suspend fun reloadConfigs(): ConfigProvider.Result = coroutineScope {
        if (!dataFolder.exists()) {
            dataFolder.toFile().mkdirs()
        }

        val arenas = dataFolder.resolve("arenas").toFile()
        if (!arenas.exists()) {
            arenas.mkdirs()
        }

        arenaConfigProvider.clear()
        val futures = ArrayList<Deferred<ConfigProvider.Result>>()
        for (file in arenas.listFiles { _, name -> name.endsWith(".yml") }!!) {
            val provider = ConfigProvider(ArenaDataConfig::class)
            scope.inject(provider)
            futures.add(async { provider.reloadConfig(file.toPath()) })
        }
        val res1 = async { configConfigProvider.reloadConfig(dataFolder.resolve("config.yml")) }
        val res2 = async { databaseConfigProvider.reloadConfig(dataFolder.resolve("database.yml")) }

        return@coroutineScope ConfigProvider.Result.combine(futures.awaitAll() + res1.await() + res2.await())
    }

}

@PitPluginScope
class ConfigProvider<T : Any>(val configClass: KClass<T>) {

    companion object {
        private val CONFIGURATION_OPTIONS = ConfigurationOptions.Builder()
            .addSerialiser(DurationSerializer)
            .addSerialiser(GameRuleSerializer)
            .addSerialiser(Pos3DSerializer)
            .setCreateSingleElementCollections(true)
            .build()
        private val SNAKE_YAML_OPTIONS = SnakeYamlOptions.Builder()
            .yamlSupplier { Yaml() }
            .commentMode(CommentMode.alternativeWriter())
            .charset(Charsets.UTF_8)
            .build()
    }

    @Inject
    lateinit var logger: Logger

    lateinit var config: T
        private set

    suspend fun reloadConfig(path: Path): Result = coroutineScope {
        val factory = SnakeYamlConfigurationFactory.create(configClass.java, CONFIGURATION_OPTIONS, SNAKE_YAML_OPTIONS)
        val defaults = factory.loadDefaults()

        return@coroutineScope runCatching {
            if (!path.parent.exists()) {
                path.parent.toFile().mkdirs()
            }

            if (!path.exists()) {
                FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
                    factory.write(defaults, it)
                }
                config = defaults
                return@runCatching Result.SUCCESS_WITH_DEFAULTS
            }

            FileChannel.open(path, StandardOpenOption.READ).use {
                runCatching {
                    config = factory.load(it)
                }.onFailure {
                    if (it is ConfigFormatSyntaxException) {
                        logger.warning("Syntax error in config file: ${path.toAbsolutePath()}")
                    } else if (it is InvalidConfigException) {
                        logger.warning("Invalid value for a variable in config file: ${path.toAbsolutePath()}")
                    }
                    return@runCatching Result.INVALID_FORMAT
                }

                if (config is AuxiliaryKeys) {
                    FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use {
                        factory.write(config, it)
                    }
                }
            }

            Result.SUCCESS
        }.getOrElse {
            Result.IO_ERROR
        }
    }

    enum class Result {
        SUCCESS_WITH_DEFAULTS,
        SUCCESS,
        INVALID_FORMAT,
        IO_ERROR;

        companion object {
            fun combine(vararg results: Result): Result {
                return combine(results.toList())
            }

            fun combine(results: Collection<Result>): Result {
                return values()[results.maxOfOrNull { it.ordinal } ?: 0]
            }
        }

        fun isSuccess(): Boolean = this == SUCCESS || this == SUCCESS_WITH_DEFAULTS
    }

}