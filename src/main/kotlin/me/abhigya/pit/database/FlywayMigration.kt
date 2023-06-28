package me.abhigya.pit.database

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import java.sql.SQLException
import javax.sql.DataSource

class FlywayMigration(
    val vendor: Vendor,
    val dataSource: DataSource
) {

    fun migrate() {
        try {
            create().migrate()
        } catch (e: FlywayException) {
            throw SQLException("Failed to migrate database", e)
        }
    }

    fun create(): Flyway {
        return Flyway.configure(javaClass.classLoader)
            .dataSource(dataSource)
            .placeholders(mapOf(
                "table_prefix" to "pit_",
                "uuidtype" to vendor.uuidType,
                "options" to vendor.options
            )).locations("classpath:db/migration")
            .validateMigrationNaming(true)
            .baselineOnMigrate(true)
            .baselineVersion("0.0")
            .load()
    }

}