package me.abhigya.pit.database

import me.abhigya.pit.configuration.DataBaseSettingsConfig
import java.util.logging.Logger

class DatabaseSettingsValidator(
    private val config: DataBaseSettingsConfig,
    private val logger: Logger
) {

    lateinit var effectiveVendor: Vendor
        private set

    fun validate() {
        val username = config.authDetails().username()
        val password = config.authDetails().password()
        effectiveVendor = if (config.vendor().isRemote() && username == "default" || password == "default") {
            logger.warning("The username and/or password for the database is not set! Defaulting to local database!")
            Vendor.HSQLDB
        } else {
            config.vendor()
        }
    }

}