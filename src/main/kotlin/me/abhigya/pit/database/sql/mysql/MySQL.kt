package me.abhigya.pit.database.sql.mysql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.abhigya.pit.database.DatabaseType
import me.abhigya.pit.database.sql.SQLDatabase
import java.sql.SQLException
import java.sql.SQLTimeoutException

/**
 * Class for interacting with a MySQL database.
 */
class MySQL(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String,
    private val params: String,
    config: HikariConfig = HikariConfig()
) : SQLDatabase(DatabaseType.MYSQL, config) {

    /**
     * Constructs the MySQL database.
     *
     * @param host     Host name
     * @param port     Port number
     * @param database Database name
     * @param username User name
     * @param password User password
     */
    init {
        check(host.isNotEmpty()) { "The host cannot be null or empty!" }
        check(database.isNotEmpty()) { "The database cannot be null or empty!" }
        check(username.isNotEmpty()) { "The username cannot be null!" }
        check(password.isNotEmpty()) { "The password cannot be null!" }
    }

    /**
     * Starts the connection with MySQL.
     *
     * @throws IllegalStateException if the JDBC drivers is unavailable.
     * @throws SQLException          if a database access error occurs.
     * @throws SQLTimeoutException   when the driver has determined that the timeout has been exceeded
     * and has at least tried to cancel the current database connection attempt.
     */
    @Synchronized
    @Throws(IllegalStateException::class, SQLException::class, SQLTimeoutException::class)
    override fun connect() {
        var driverClass = DRIVER_CLASS
        try {
            Class.forName(DRIVER_CLASS)
        } catch (ex: ClassNotFoundException) {
            driverClass = try {
                Class.forName(LEGACY_DRIVER_CLASS)
                LEGACY_DRIVER_CLASS
            } catch (ex2: ClassNotFoundException) {
                throw IllegalStateException("Could not connect to MySQL! The JDBC driver is unavailable!")
            }
        }
        if (dataSource == null) {
            config.driverClassName = driverClass
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
        private const val URL_FORMAT = ("jdbc:mysql://"
                + "%s" // host
                + ":"
                + "%d" // port
                + "/"
                + "%s") // database

        /**
         * The JDBC driver class.
         */
        private const val DRIVER_CLASS = "com.mysql.cj.jdbc.Driver"
        private const val LEGACY_DRIVER_CLASS = "com.mysql.jdbc.Driver"
    }
}
