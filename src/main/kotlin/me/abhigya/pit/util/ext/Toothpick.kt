package me.abhigya.pit.util.ext

import me.abhigya.pit.ThePitPlugin
import toothpick.Scope
import toothpick.Toothpick
import toothpick.ktp.KTP
import java.util.logging.Logger

fun Any.injectMembers() = Toothpick.inject(this, scope)

val scope: Scope = KTP.openScope(ThePitPlugin::class.java)

inline fun <reified T> logger(): Logger = Logger.getLogger(T::class.java.name)