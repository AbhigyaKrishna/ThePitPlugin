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

val Vendor.initSql: String get() {
    fun sqlModes(vararg modes: String): String {
        return modes.joinToString(separator = ",", prefix = "SET @@SQL_MODE = CONCAT(@@SQL_MODE, ',", postfix = "')")
    }

    return when(this) {
        Vendor.HSQLDB -> "SET DATABASE TRANSACTION CONTROL MVLOCKS"
        Vendor.MARIADB -> "SET NAMES utf8mb4 COLLATE utf8mb4_bin; " + sqlModes(
            // MariaDB defaults
            // Specify explicitly so that unwise shared hosts do not cause issues
            "STRICT_TRANS_TABLES",
            "ERROR_FOR_DIVISION_BY_ZERO",
            "NO_AUTO_CREATE_USER",
            "NO_ENGINE_SUBSTITUTION",
            // Modes specifically used by LibertyBans, for better ANSI SQL compliance
            "ANSI",
            "NO_BACKSLASH_ESCAPES",
            "SIMULTANEOUS_ASSIGNMENT", // MDEV-13417
            "NO_ZERO_IN_DATE",
            "NO_ZERO_DATE"
        )
        Vendor.MYSQL -> "SET NAMES utf8mb4 COLLATE utf8mb4_bin; " + sqlModes(
            // MySQL defaults, set explicitly
            "STRICT_TRANS_TABLES",
            "ERROR_FOR_DIVISION_BY_ZERO",
            "NO_ENGINE_SUBSTITUTION",
            // Modes specifically used by LibertyBans
            "ANSI",
            "NO_BACKSLASH_ESCAPES",
            "NO_ZERO_IN_DATE",
            "NO_ZERO_DATE"
        )
        Vendor.POSTGRESQL -> "SET NAMES 'UTF8'"
    }
}
