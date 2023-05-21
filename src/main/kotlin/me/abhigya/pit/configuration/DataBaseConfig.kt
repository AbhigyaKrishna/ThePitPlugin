package me.abhigya.pit.configuration

import me.abhigya.pit.database.DatabaseType
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class DataBaseConfig {

    var databaseType: DatabaseType = DatabaseType.H2

    var host: String = "localhost"
    var port: Int = 3306
    var database: String = "database"
    var username: String = "username"
    var password: String = "password"
    var params: String = "?useSSL=true"

}