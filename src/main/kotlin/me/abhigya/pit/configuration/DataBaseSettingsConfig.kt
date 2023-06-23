package me.abhigya.pit.configuration

import me.abhigya.pit.database.Vendor
import space.arim.dazzleconf.annote.ConfComments
import space.arim.dazzleconf.annote.ConfDefault
import space.arim.dazzleconf.annote.ConfKey
import space.arim.dazzleconf.annote.SubSection
import kotlin.time.Duration

interface DataBaseSettingsConfig {

    @ConfKey("rdbms-vendor")
    @ConfComments("The RDBMS vendor to use",
        "Supported vendors | Min. version | Description",
        "HSQLDB            | 2.7          | Local database, selected by default",
        "MySQL             | 8.0          | Remote database",
        "MARIADB           | 10.6         | Remote database",
        "POSTGRESQL        | 12           | Remote database"
    )
    @ConfDefault.DefaultString("HSQLDB")
    fun vendor(): Vendor

    @ConfKey("connection-pool-size")
    @ConfComments("The maximum number of connections in the pool")
    @ConfDefault.DefaultInteger(6)
    fun connectionPoolSize(): Int

    @ConfKey("connection-timeout")
    @ConfComments("The maximum number of seconds to wait for a connection to become available")
    @ConfDefault.DefaultString("30s")
    fun connectionTimeout(): Duration

    @ConfKey("max-lifetime")
    @ConfComments("The maximum lifetime of a connection in the pool.",
        "This value should be set for MariaDB or MySQL.",
        "HikariCP notes: It should be several seconds shorter than any database or infrastructure imposed connection time limit")
    @ConfDefault.DefaultString("25m")
    fun maxLifetime(): Duration

    @ConfKey("auth-details")
    @SubSection
    @ConfComments("The authentication details for the database.",
        "Only applicable for remote databases")
    fun authDetails(): AuthDetails

    interface AuthDetails {

        @ConfDefault.DefaultString("localhost")
        fun host(): String

        @ConfDefault.DefaultInteger(3306)
        fun port(): Int

        @ConfDefault.DefaultString("pit")
        fun database(): String

        @ConfKey("user")
        @ConfDefault.DefaultString("defaultuser")
        fun username(): String

        @ConfDefault.DefaultString("defaultpass")
        fun password(): String

    }

    @ConfKey("mariadb-config")
    @SubSection
    @ConfComments("Only applicable for MariaDB")
    fun mariaDbConfig(): MariaDbConfig

    interface MariaDbConfig {

        @ConfKey("connection-properties")
        @ConfComments("The connection properties for the database.")
        @ConfDefault.DefaultMap(
            "useUnicode", "true",
            "characterEncoding", "UTF-8",
            "useServerPrepStmts", "true",
            "cachePrepStmts", "true",
            "prepStmtCacheSize", "25",
            "prepStmtCacheSqlLimit", "1024",
        )
        fun connectionProperties(): Map<String, String>

    }

    @ConfKey("postgres-config")
    @SubSection
    @ConfComments("Only applicable for PostgreSQL")
    fun postGresConfig(): PostGresConfig

    interface PostGresConfig {

        @ConfKey("connection-properties")
        @ConfComments("The connection properties for the database.")
        @ConfDefault.DefaultMap(
            "preparedStatementCacheQueries", "25"
        )
        fun connectionProperties(): Map<String, String>

    }

}