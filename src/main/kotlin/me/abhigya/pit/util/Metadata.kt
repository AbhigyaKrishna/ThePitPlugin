package me.abhigya.pit.util

import me.abhigya.pit.ThePitPlugin
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.Plugin

private val plugin = ThePitPlugin.getPlugin()
typealias Metadata = Pair<String, MetadataValue>

val BREAKABLE_BLOCK_METADATA: Metadata = "breakable" to NullMetadataValue

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

fun Block.hasMetaData(metaData: Metadata): Boolean {
    return getMetadata(metaData.first).any {
        if (it is FixedMetadataValue) {
            it.value() == metaData.second.value()
        } else {
            it == metaData.second
        }
    }
}