package me.abhigya.pit.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.abhigya.pit.database.Database
import me.abhigya.pit.database.DatabaseType
import me.abhigya.pit.database.NullConnection
import java.sql.*

abstract class SQLDatabase(
    type: DatabaseType,
    protected val config: HikariConfig
) : Database(type) {

    protected var dataSource: HikariDataSource? = null

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

    private var retries = 5

    override val isConnected: Boolean
        get() = try {
            connection != NullConnection && !connection.isClosed && connection.isValid(3)
        } catch (e: SQLException) {
            false
        }

    @Throws(SQLException::class)
    override fun disconnect() {
        check(isConnected) { "Not connected!" }
        connection.close()
        connection = NullConnection
        dataSource?.close()
    }

    @Throws(SQLException::class)
    suspend fun execute(query: String): Boolean {
        return connection.prepareStatement(query).use {
            it.execute()
        }
    }

    @Throws(SQLException::class)
    suspend fun query(query: String): ResultSet {
        return connection.prepareStatement(query).use {
            it.executeQuery()
        }
    }

    @Throws(SQLException::class)
    suspend fun update(update: String): Int {
        return connection.prepareStatement(update).use {
            it.executeUpdate()
        }
    }

}
