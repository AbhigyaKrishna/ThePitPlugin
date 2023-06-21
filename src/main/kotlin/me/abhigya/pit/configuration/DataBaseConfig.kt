package me.abhigya.pit.configuration

import me.abhigya.pit.database.Vendor
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class DataBaseConfig(
    val vendor: Vendor,
    val host: String,
    val port: Int = 3306,
    val database: String,
    val username: String,
    val password: String,
    val params: String
)