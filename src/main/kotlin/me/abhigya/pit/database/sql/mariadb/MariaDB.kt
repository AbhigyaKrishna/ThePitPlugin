package me.abhigya.pit.database.sql.mariadb

import me.abhigya.pit.configuration.DataBaseSettingsConfig
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.sql.SQLDatabase

/**
 * Class for interacting with a MySQL database.
 */
class MariaDB(
    vendor: Vendor, // MariaDB or MySQL
    settings: DataBaseSettingsConfig
) : SQLDatabase(vendor, settings) {

    init {
        require(vendor == Vendor.MARIADB || vendor == Vendor.MYSQL) { "Vendor must be MariaDB or MySQL" }
    }

    override val props: Map<String, Any> = buildMap {
        // Performance improvements
        this["autocommit"] = AUTOCOMMIT
        this["defaultFetchSize"] = FETCH_SIZE

        // Help debug in case of deadlock
        this["includeInnodbStatusInDeadlockExceptions"] = true
        this["includeThreadDumpInDeadlockExceptions"] = true

        // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery#mysql
        this["socketTimeout"] = SOCKET_TIMEOUT

        putAll(settings.mariaDbConfig().connectionProperties())

        // Needed for use with connection init-SQL (hikariConf.setConnectionInitSql)
        this["allowMultiQueries"] = true
        // Help debug in case of exceptions
        this["dumpQueriesOnException"] = true
    }

    override val url: String = run {
        val credentials = settings.authDetails()
        "jdbc:mariadb://${credentials.host()}:${credentials.port()}/${credentials.database()}"
    }

}
