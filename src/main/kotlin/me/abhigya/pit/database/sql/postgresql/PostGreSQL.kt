package me.abhigya.pit.database.sql.postgresql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.abhigya.pit.database.DatabaseType
import me.abhigya.pit.database.sql.SQLDatabase
import java.sql.SQLException

class PostGreSQL(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String,
    private val params: String,
    config: HikariConfig = HikariConfig()
) : SQLDatabase(DatabaseType.PostGreSQL, config) {

    init {
        check(host.isNotEmpty()) { "The host cannot be null or empty!" }
        check(database.isNotEmpty()) { "The database cannot be null or empty!" }
        check(username.isNotEmpty()) { "The username cannot be null!" }
        check(password.isNotEmpty()) { "The password cannot be null!" }
    }

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
