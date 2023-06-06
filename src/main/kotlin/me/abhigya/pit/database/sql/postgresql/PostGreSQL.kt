package me.abhigya.pit.database.sql.postgresql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.sql.SQLDatabase
import java.sql.SQLException

class PostGreSQL(config: HikariConfig = HikariConfig()) : SQLDatabase(Vendor.PostGreSQL, config) {

    @Synchronized
    @Throws(IllegalStateException::class, SQLException::class)
    override fun connect() {
        runCatching {
            Class.forName(DRIVER_CLASS)
        }.onFailure {
            throw IllegalStateException("Could not connect to PostGreSQL! The JDBC driver is unavailable!")
        }
        if (dataSource == null) {
            config.driverClassName = DRIVER_CLASS
            config.jdbcUrl = String.format(URL_FORMAT, host, port, database) + params
            config.username = username
            config.password = password
            dataSource = HikariDataSource(config)
        }
        connection = dataSource!!.connection
    }

    companion object {
        /**
         * Connection URL format.
         */
        private const val URL_FORMAT = ("jdbc:postgresql://"
                + "%s" // host
                + ":"
                + "%d" // port
                + "/"
                + "%s") // database
        private const val DRIVER_CLASS = "org.postgresql.Driver"
    }
}
