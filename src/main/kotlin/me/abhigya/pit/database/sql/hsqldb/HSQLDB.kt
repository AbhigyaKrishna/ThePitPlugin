package me.abhigya.pit.database.sql.hsqldb

import me.abhigya.pit.configuration.DataBaseSettingsConfig
import me.abhigya.pit.database.Vendor
import me.abhigya.pit.database.sql.SQLDatabase
import java.nio.file.Path
import java.sql.SQLException
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class HSQLDB(
    private val path: Path,
    settings: DataBaseSettingsConfig
) : SQLDatabase(Vendor.HSQLDB, settings) {

    override val props: Map<String, Any> = mapOf(
        // Prevent execution of multiple queries in one Statement
        "sql.restrict_exec" to true,
        // Make the names of generated indexes the same as the names of the constraints
        "sql.sys_index_names" to true,
        /*
         * Enforce SQL standards on
         * 1.) table and column names
         * 2.) ambiguous column references
         * 3.) illegal type conversions
         */
        "sql.enforce_names" to true,
        "sql.enforce_refs" to true,
        "sql.enforce_types" to true,
        // Respect interrupt status during query execution
        "hsqldb.tx_interrupt_rollback" to true,
        // Use CACHED tables by default
        "hsqldb.default_table_type" to "cached",
    )

    override val url: String = "jdbc:hsqldb:file:${path.toAbsolutePath()}"

    override fun setUsernameAndPassword() {
        config.username = "SA"
        config.password = ""
    }

    @Synchronized
    @Throws(IllegalStateException::class, SQLException::class)
    override fun connect() {
        if (!path.parent.isDirectory()) {
            path.parent.createDirectories()
        }

        if (!path.exists()) {
            path.createFile()
        }

        super.connect()
    }

}