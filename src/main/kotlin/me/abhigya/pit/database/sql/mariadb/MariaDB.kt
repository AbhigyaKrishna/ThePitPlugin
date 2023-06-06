package me.abhigya.pit.database.sql.mariadb

import com.zaxxer.hikari.HikariConfig
import me.abhigya.pit.database.DatabaseSettings
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.formatConnectionProperties
import me.abhigya.pit.database.sql.SQLDatabase

/**
 * Class for interacting with a MySQL database.
 */
class MariaDB(
    vendor: Vendor,
    settings: DatabaseSettings,
    config: HikariConfig = HikariConfig()
) : SQLDatabase(vendor, settings, config) {

//    /**
//     * Starts the connection with MySQL.
//     *
//     * @throws IllegalStateException if the JDBC drivers is unavailable.
//     * @throws SQLException          if a database access error occurs.
//     * @throws SQLTimeoutException   when the driver has determined that the timeout has been exceeded
//     * and has at least tried to cancel the current database connection attempt.
//     */
//    @Synchronized
//    @Throws(IllegalStateException::class, SQLException::class, SQLTimeoutException::class)
//    override fun connect() {
//        assert(vendor == Vendor.MARIADB || vendor == Vendor.MYSQL)
//        val url = "jdbc:mariadb://${credentials.host}:${credentials.port}/${credentials.database}${vendor.jdbcDriver.formatConnectionProperties(props)}"
//
//        config.jdbcUrl = url
//        config.username = credentials.username
//        config.password = credentials.password
//        config.driverClassName = vendor.jdbcDriver.jdbcDriverClass
//        config.dataSourceClassName = vendor.jdbcDriver.dataSourceClass
//    }

    override fun prepare() {
        assert(vendor == Vendor.MARIADB || vendor == Vendor.MYSQL)
        val url = "jdbc:mariadb://${settings.credentials.host}:${settings.credentials.port}/${settings.credentials.database}${vendor.jdbcDriver.formatConnectionProperties(props)}"

        config.jdbcUrl = url

        val props = mapOf(
            // Performance improvements
            "autocommit" to AUTOCOMMIT,
            "defaultFetchSize" to FETCH_SIZE,

            // Help debug in case of deadlock
            "includeInnodbStatusInDeadlockExceptions" to true,
            "includeThreadDumpInDeadlockExceptions" to true,

            // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery#mysql
            "socketTimeout" to SOCKET_TIMEOUT
        )
    }
}
