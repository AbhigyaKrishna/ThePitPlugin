package me.abhigya.pit.database.binding

import org.jooq.*
import org.jooq.impl.AbstractBinding
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException


abstract class BaseBinding<T, U> : AbstractBinding<T, U>() {
    @Throws(SQLException::class)
    abstract operator fun set(dialect: SQLDialect, statement: PreparedStatement, index: Int, value: U)

    @Throws(SQLException::class)
    abstract operator fun get(dialect: SQLDialect, resultSet: ResultSet, index: Int): U

    abstract fun inline(dialect: SQLDialect, value: U): Field<*>

    @Throws(SQLException::class)
    override fun set(ctx: BindingSetStatementContext<U>) {
        set(ctx.family(), ctx.statement(), ctx.index(), ctx.value())
    }

    @Throws(SQLException::class)
    override fun get(ctx: BindingGetResultSetContext<U>) {
        val value = get(ctx.dialect(), ctx.resultSet(), ctx.index())
        ctx.value(value)
    }

    @Throws(SQLException::class)
    override fun sqlInline(ctx: BindingSQLContext<U>) {
        val inlined = inline(ctx.family(), ctx.value())
        ctx.render().visit(inlined)
    }
}