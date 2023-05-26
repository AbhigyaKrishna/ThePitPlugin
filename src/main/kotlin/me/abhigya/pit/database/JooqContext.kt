package me.abhigya.pit.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.abhigya.jooq.codegen.tables.Players
import me.abhigya.jooq.codegen.tables.records.PlayersRecord
import me.abhigya.pit.ThePitPlugin
import me.abhigya.pit.database.sql.SQLDatabase
import me.abhigya.pit.util.ext.scope
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

val MATCH_ALL_EXCEPT_INFORMATION_SCHEMA = Regex("^(?!INFORMATION_SCHEMA)(.*?)$").toPattern()
val MATCH_ALL = Regex("^(.*?)\$").toPattern()
const val REPLACEMENT ="pit_$0"

class JooqContext(val dialect: SQLDialect) {

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

val DatabaseType.dialect: SQLDialect
    get() = when (this) {
        DatabaseType.H2 -> SQLDialect.H2
        DatabaseType.MYSQL -> SQLDialect.MYSQL
        DatabaseType.PostGreSQL -> SQLDialect.POSTGRES
    }

val SQLDatabase.context: JooqContext get() = JooqContext(databaseType.dialect)

@OptIn(ExperimentalContracts::class)
inline fun databaseTransaction(crossinline block: JooqContext.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    val scope = scope
    scope.getInstance<ThePitPlugin>().launch(Dispatchers.IO) {
        block(scope.getInstance<SQLDatabase>().context)
    }
}

suspend fun JooqContext.fetchPlayerData(context: DSLContext, uuid: UUID): PlayersRecord = coroutineScope {
    context.fetch(Players.PLAYERS, Players.PLAYERS.UUID.eq(uuid.toString())).first()
}