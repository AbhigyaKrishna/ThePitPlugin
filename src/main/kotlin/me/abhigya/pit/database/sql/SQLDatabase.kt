package me.abhigya.pit.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.coroutineScope
import me.abhigya.pit.database.Database
import me.abhigya.pit.database.DatabaseSettings
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.NullConnection
import java.sql.*

abstract class SQLDatabase(
    vendor: Vendor,
    protected val settings: DatabaseSettings,
    protected val config: HikariConfig
) : Database(vendor) {

    companion object {
        const val AUTOCOMMIT: Boolean = false
        const val FETCH_SIZE: Int = 1000
        const val SOCKET_TIMEOUT: Int = 30000
    }

    protected var dataSource: HikariDataSource? = null
    private var retries = 5

    var connection: Connection = NullConnection
        @Throws(IllegalStateException::class, SQLException::class)
        get() {
            var trys = 0
            var exception: SQLTimeoutException? = null
            while (trys < retries) {
                try {
                    if (!isConnected) {
                        field = dataSource!!.connection
                    }
                    return field
                } catch (e: SQLTimeoutException) {
                    trys++
                    exception = e
                }
            }
            val throwable = SQLException("Could not get connection after $retries tries!")
            if (exception != null) {
                throwable.nextException = exception
            }
            throw throwable
        }
        protected set

    override val isConnected: Boolean
        get() = try {
            connection != NullConnection && !connection.isClosed && connection.isValid(3)
        } catch (e: SQLException) {
            false
        }

    protected abstract fun prepare()

    @Throws(SQLException::class)
    override fun connect() {
        prepare()

        config.username = settings.credentials.username
        config.password = settings.credentials.password
        config.driverClassName = vendor.jdbcDriver.jdbcDriverClass
        config.dataSourceClassName = vendor.jdbcDriver.dataSourceClass
    }

    @Throws(SQLException::class)
    override fun disconnect() {
        check(isConnected) { "Not connected!" }
        connection.close()
        connection = NullConnection
        dataSource?.close()
    }

    protected fun createDataSource() {
        dataSource = HikariDataSource(config)
    }

    @Throws(SQLException::class)
    suspend fun execute(query: String): Boolean = coroutineScope {
        connection.prepareStatement(query).use {
            it.execute()
        }
    }

    @Throws(SQLException::class)
    suspend fun query(query: String): ResultSet = coroutineScope {
        connection.prepareStatement(query).use {
            it.executeQuery()
        }
    }

    @Throws(SQLException::class)
    suspend fun update(update: String): Int = coroutineScope {
        connection.prepareStatement(update).use {
            it.executeUpdate()
        }
    }

}
