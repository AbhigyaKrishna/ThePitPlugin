package me.abhigya.pit.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

val SQLDatabase.context: JooqContext get() = JooqContext(vendor.dialect)

class Transaction(
    val context: JooqContext,
    val connection: Connection
) : DSLContext by context.createContext(connection)

@OptIn(ExperimentalContracts::class)
inline fun transaction(
    coroutineScope: CoroutineScope = ThePitPlugin.getPlugin(),
    database: SQLDatabase = ThePitPlugin.getPlugin().scope.getInstance<SQLDatabase>(),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline block: suspend Transaction.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    coroutineScope.launch(dispatcher) {
        val jooqContext = database.context
        block(Transaction(jooqContext, database.connection))
    }
}