package me.abhigya.pit.database.sql.h2

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.abhigya.pit.database.DatabaseType
import me.abhigya.pit.database.sql.SQLDatabase
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.*

class H2(
    private val db: File,
    config: HikariConfig = HikariConfig()
) : SQLDatabase(DatabaseType.H2, config) {

    @Synchronized
    @Throws(IOException::class, IllegalStateException::class, SQLException::class)
    override fun connect() {
        check(db.name.endsWith(".db")) { "The database file should have '.db' extension." }

        if (!db.parentFile.exists()) db.parentFile.mkdirs()
        if (!db.exists()) db.createNewFile()

        runCatching {
            Class.forName(DRIVER_CLASS)
        }.onFailure {
            throw IllegalStateException("Could not connect to H2! The H2 driver is unavailable!")
        }

        if (dataSource == null) {
            config.dataSourceClassName = DRIVER_CLASS
            val properties = Properties()
            properties["URL"] = "jdbc:h2:file:" + db.absolutePath
            config.dataSourceProperties = properties
            dataSource = HikariDataSource(config)
        }
        connection = dataSource!!.connection
    }

    companion object {
        /**
         * The JDBC driver class.
         */
        private const val DRIVER_CLASS = "org.h2.jdbcx.JdbcDataSource"
    }
}
