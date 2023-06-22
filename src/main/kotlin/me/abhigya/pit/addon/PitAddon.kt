package me.abhigya.pit.addon

import toothpick.Scope
import toothpick.ktp.extension.getInstance
import java.nio.file.Path
import java.util.logging.Logger

abstract class PitAddon(
    val scope: Scope
) {

    internal var isEnabled: Boolean = false

    val dataFolder: Path get() = scope.getInstance<Path>("dataFolder")

    val logger: Logger = Logger.getLogger(javaClass.simpleName)

    val description: AddonDescription = scope.getInstance()

    val name get() = description.name()

    fun initiate() {

    }

    fun disable() {

    }

}