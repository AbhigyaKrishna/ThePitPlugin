package me.abhigya.pit.configuration

import org.bukkit.plugin.Plugin
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import toothpick.InjectConstructor

@InjectConstructor
class Configuration(
    private val plugin: Plugin
) {

    lateinit var config: MainConfig
    lateinit var database: DataBaseConfig

    fun saveDefaults() {
        YamlConfigurationLoader.builder()
            .path(this.plugin.dataFolder.toPath().resolve("config.yml"))
            .build().let {
                it.save(it.createNode().set(MainConfig::class, MainConfig()))
            }

        YamlConfigurationLoader.builder()
            .path(this.plugin.dataFolder.toPath().resolve("database.yml"))
            .build().let {
                it.save(it.createNode().set(DataBaseConfig::class, DataBaseConfig()))
            }
    }

    fun load() {
        config = YamlConfigurationLoader.builder()
            .path(this.plugin.dataFolder.toPath().resolve("config.yml"))
            .build()
            .load()
            .get<MainConfig> { MainConfig() }
        database = YamlConfigurationLoader.builder()
            .path(this.plugin.dataFolder.toPath().resolve("database.yml"))
            .build()
            .load()
            .get<DataBaseConfig> { DataBaseConfig() }
    }

}