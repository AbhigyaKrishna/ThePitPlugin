package me.abhigya.pit.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.abhigya.jooq.codegen.tables.records.PlayersRecord
import me.abhigya.jooq.codegen.tables.records.StatsRecord
import me.abhigya.jooq.codegen.tables.references.PLAYERS
import me.abhigya.jooq.codegen.tables.references.STATS
import me.abhigya.pit.ThePitPlugin
import me.abhigya.pit.database.sql.SQLDatabase
import org.jooq.ConnectionProvider
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.MappedTable
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.NoConnectionProvider
import toothpick.ktp.extension.getInstance
import java.sql.Connection
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class JooqContext(private val dialect: SQLDialect) {

    companion object {
        val MATCH_ALL_EXCEPT_INFORMATION_SCHEMA = Regex("^(?!INFORMATION_SCHEMA)(.*?)$").toPattern()
        val MATCH_ALL = Regex("^(.*?)\$").toPattern()
        const val REPLACEMENT ="pit_$0"
    }

    fun createContext(connection: Connection): DSLContext {
        class ConnectionProviderImpl : ConnectionProvider {
            override fun acquire(): Connection {
                return connection
            }

            override fun release(connection: Connection) {
                // do nothing
            }
        }

        return createWith(ConnectionProviderImpl())
    }

    fun createDummyContext(): DSLContext {
        return createWith(NoConnectionProvider())
    }

    private fun createWith(provider: ConnectionProvider): DSLContext {
        return DefaultConfiguration()
                .set(provider)
                .set(dialect)
                .set(createSettings())
                .dsl()
    }

    private fun createSettings(): Settings {
        return Settings()
                .withRenderSchema(false)
                .withRenderMapping(RenderMapping()
                        .withSchemata(MappedSchema()
                                .withInputExpression(MATCH_ALL_EXCEPT_INFORMATION_SCHEMA)
                                .withTables(MappedTable()
                                        .withInputExpression(MATCH_ALL)
                                        .withOutput(REPLACEMENT))))
    }
}

val Vendor.dialect: SQLDialect
    get() = when (this) {
        Vendor.HSQLDB -> SQLDialect.HSQLDB
        Vendor.MYSQL -> SQLDialect.MYSQL
        Vendor.MARIADB -> SQLDialect.MARIADB
        Vendor.POSTGRESQL -> SQLDialect.POSTGRES
    }

val Vendor.uuidType
    get() = when (this) {
        Vendor.HSQLDB, Vendor.POSTGRESQL -> "UUID"
        Vendor.MYSQL, Vendor.MARIADB -> "BINARY(16)"
    }

val SQLDatabase.context: JooqContext get() = JooqContext(vendor.dialect)

@OptIn(ExperimentalContracts::class)
inline fun databaseTransaction(crossinline block: suspend JooqContext.(DSLContext) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    val plugin = ThePitPlugin.getPlugin()
    plugin.launch(Dispatchers.IO) {
        val database = plugin.scope.getInstance<SQLDatabase>()
        val jooqContext = database.context
        block(jooqContext, jooqContext.createContext(database.connection))
    }
}

suspend fun JooqContext.fetchPlayerData(context: DSLContext, uuid: UUID): PlayersRecord = coroutineScope {
    context.fetch(PLAYERS, PLAYERS.UUID.eq(uuid)).first()
}

suspend fun JooqContext.fetchPlayerStats(context: DSLContext, uuid: UUID): StatsRecord = coroutineScope {
    context.fetch(STATS, STATS.UUID.eq(uuid)).first()
}