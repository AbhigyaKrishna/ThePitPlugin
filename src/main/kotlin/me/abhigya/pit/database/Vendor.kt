package me.abhigya.pit.database

enum class Vendor(
    val display: String,
    val jdbcDriver: JDBCDriver
) {
    HSQLDB("HyperSQL", JDBCDriver.HSQLDB),
    MYSQL("MySQL", JDBCDriver.MARIADB),
    MARIADB("MariaDB", JDBCDriver.MARIADB),
    POSTGRESQL("PostGreSQL", JDBCDriver.PostGreSQL)
    ;

    fun isLocal(): Boolean = this == HSQLDB

    fun isRemote(): Boolean = !isLocal()
}

enum class JDBCDriver(
    val jdbcDriverClass: String,
    val dataSourceClass: String,
    val urlPropertyPrefix: Char,
    val urlPropertySeparator: Char,
) {
    HSQLDB("org.hsqldb.jdbc.JDBCDriver", "org.hsqldb.jdbc.JDBCDataSource", ';', ';'),
    MARIADB("org.mariadb.jdbc.Driver", "org.mariadb.jdbc.MariaDbDataSource", '?', '&'),
    PostGreSQL("org.postgresql.Driver", "org.postgresql.ds.PGSimpleDataSource", '?', '&'),
}

fun JDBCDriver.formatConnectionProperties(props: Map<String, Any>): String {
    if (props.isEmpty()) return ""

    return props.map { (k, v) -> "$k=$v" }
        .joinToString(separator = urlPropertySeparator.toString(), prefix = urlPropertyPrefix.toString())
}
