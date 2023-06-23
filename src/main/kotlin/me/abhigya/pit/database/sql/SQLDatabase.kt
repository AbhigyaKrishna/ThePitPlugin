package me.abhigya.pit.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.coroutineScope
import me.abhigya.pit.configuration.DataBaseSettingsConfig
import me.abhigya.pit.database.*
import java.sql.*

abstract class SQLDatabase(
    vendor: Vendor,
    protected val settings: DataBaseSettingsConfig
) : Database(vendor) {

    companion object {
        const val AUTOCOMMIT: Boolean = false
        const val FETCH_SIZE: Int = 1000
        const val SOCKET_TIMEOUT: Int = 30000
    }

    protected val config = HikariConfig()
    protected var dataSource: HikariDataSource? = null
    private var retries = 5

    var connection: Connection = NullConnection
        @Throws(IllegalStateException::class, SQLException::class)
        get() {
            if (dataSource == null) {
                throw IllegalStateException("Database has not been initialized yet!")
            }
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

    abstract val props: Map<String, Any>

    abstract val url: String

    protected fun setDriverClassName(driverClass: String) {
        val currentThread = Thread.currentThread()
        val initialClassLoader = currentThread.contextClassLoader
        currentThread.contextClassLoader = javaClass.classLoader
        try {
            config.driverClassName = driverClass
        } finally {
            currentThread.contextClassLoader = initialClassLoader
        }
    }

    protected open fun setUsernameAndPassword() {
        val credentials = settings.authDetails()
        config.username = credentials.username()
        config.password = credentials.password()
    }

    @Synchronized
    @Throws(SQLException::class)
    override fun connect() {
        config.jdbcUrl = url + vendor.jdbcDriver.formatConnectionProperties(props)
        setUsernameAndPassword()
        setDriverClassName(vendor.jdbcDriver.jdbcDriverClass)

        config.connectionTimeout = settings.connectionTimeout().inWholeMilliseconds
        config.maxLifetime = settings.maxLifetime().inWholeMilliseconds

        val poolSize = settings.connectionPoolSize()
        config.minimumIdle = poolSize
        config.maximumPoolSize = poolSize

        config.isAutoCommit = AUTOCOMMIT
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.poolName = "ThePitPool-${vendor}"
        config.connectionInitSql = vendor.initSql
        config.isIsolateInternalQueries = true

        dataSource = HikariDataSource(config)
        connection = dataSource!!.connection
    }

    @Synchronized
    @Throws(SQLException::class)
    override fun disconnect() {
        check(isConnected) { "Not connected!" }
        connection.close()
        connection = NullConnection
        dataSource?.close()
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
