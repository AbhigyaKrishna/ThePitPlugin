package me.abhigya.pit.util

import me.abhigya.pit.util.ext.scope
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import toothpick.ktp.extension.getInstance

private val plugin = scope.getInstance<JavaPlugin>()
typealias Metadata = Pair<String, MetadataValue>

val PLACED_BLOCK_METADATA: Metadata = "placed" to FixedMetadataValue(plugin, true)

object NullMetadataValue : MetadataValue {

    override fun value(): Any? = null

    override fun asInt(): Int = 0

    override fun asFloat(): Float = 0.0f

    override fun asDouble(): Double = 0.0

    override fun asLong(): Long = 0

    override fun asShort(): Short = 0

    override fun asByte(): Byte = 0

    override fun asBoolean(): Boolean = false

    override fun asString(): String = String()

    override fun getOwningPlugin(): Plugin = plugin

    override fun invalidate() {}

}