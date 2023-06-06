package me.abhigya.pit.database

import com.zaxxer.hikari.HikariConfig
import me.abhigya.pit.util.ext.logger

class DatabaseSettings(
    var credentials: DatabaseCredentials,
    val hikariConfiguration: HikariConfiguration,
    var vendor: Vendor
) {

    private val logger = logger<DatabaseSettings>()

    private fun checkCredentials() {
        if (vendor.isRemote() && credentials.username == "default" || credentials.password == "default") {
            logger.warning("The username and/or password for the database is not set! Defaulting to local database!")
            vendor = Vendor.HSQLDB
        }
        if (vendor == Vendor.HSQLDB) {
            this.credentials = this.credentials.copy(
                username = "SA",
                password = ""
            )
        }
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