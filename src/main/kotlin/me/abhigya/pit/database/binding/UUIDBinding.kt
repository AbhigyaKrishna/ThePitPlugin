package me.abhigya.pit.database.binding

import me.abhigya.pit.util.ext.fromByteArray
import me.abhigya.pit.util.ext.toByteArray
import org.jooq.BindingSQLContext
import org.jooq.Converter
import org.jooq.Field
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.UUID

class UUIDBinding : BaseBinding<UUID, UUID>() {

    override fun converter(): Converter<UUID, UUID> {
        return Converter.of(UUID::class.java, UUID::class.java, { it }, { it })
    }

    private fun supportsUUID(dialect: SQLDialect): Boolean {
        return dialect == SQLDialect.POSTGRES || dialect == SQLDialect.H2
    }

    @Throws(SQLException::class)
    override operator fun set(dialect: SQLDialect, statement: PreparedStatement, index: Int, value: UUID) {
        if (supportsUUID(dialect)) {
            statement.setObject(index, value)
        } else {
            statement.setBytes(index, value.toByteArray())
        }
    }

    @Throws(SQLException::class)
    override fun get(dialect: SQLDialect, resultSet: ResultSet, index: Int): UUID {
        return if (supportsUUID(dialect)) {
            resultSet.getObject(index, UUID::class.java)
        } else {
            fromByteArray(resultSet.getBytes(index))
        }
    }

    override fun inline(dialect: SQLDialect, value: UUID): Field<*> {
        return if (supportsUUID(dialect)) {
            DSL.inline(value, SQLDataType.UUID)
        } else {
            DSL.inline(value.toByteArray(), SQLDataType.BINARY(16))
        }
    }

    @Throws(SQLException::class)
    override fun sqlBind(ctx: BindingSQLContext<UUID>) {
        if (supportsUUID(ctx.dialect())) {
            ctx.render().sql("cast(" + ctx.variable() + " as uuid)")
            return
        }
        super.sqlBind(ctx)
    }

}