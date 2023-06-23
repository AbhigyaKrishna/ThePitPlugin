package me.abhigya.pit.database

import com.zaxxer.hikari.HikariConfig
import me.abhigya.pit.configuration.DataBaseSettingsConfig
import me.abhigya.pit.util.ext.logger

class DatabaseSettings(
    val config: DataBaseSettingsConfig,
    var vendor: Vendor
) {

    private val logger = logger<DatabaseSettings>()
    private val hikariContext = HikariConfig()

    private fun checkCredentials() {
        var username = config.authDetails().username()
        var password = config.authDetails().password()
        if (vendor.isRemote() && username == "default" || password == "default") {
            logger.warning("The username and/or password for the database is not set! Defaulting to local database!")
            vendor = Vendor.HSQLDB
        }
        if (vendor == Vendor.HSQLDB) {
            username = "SA"
            password = ""
        }
        hikariContext.username = username
        hikariContext.password = password
    }
}

data class DatabaseCredentials(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String
)

typealias HikariConfiguration = HikariConfig.() -> Unit