package me.abhigya.pit.database

abstract class Database(
    val vendor: Vendor
) {

    abstract val isConnected: Boolean

    @Throws(Exception::class)
    abstract fun connect()

    @Throws(Exception::class)
    abstract fun disconnect()

}
