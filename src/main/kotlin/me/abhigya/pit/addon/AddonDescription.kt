package me.abhigya.pit.addon

interface AddonDescription {

    fun name(): String

    fun description(): String

    fun version(): String

    fun authors(): List<String>

    fun main(): String

    fun depends(): List<String>

    fun softDepends(): List<String>

}