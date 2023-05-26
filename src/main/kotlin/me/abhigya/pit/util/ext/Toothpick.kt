package me.abhigya.pit.util.ext

import me.abhigya.pit.ThePitPlugin
import toothpick.Scope
import toothpick.Toothpick
import toothpick.ktp.KTP

fun Any.injectMembers() = Toothpick.inject(this, KTP.openScope(ThePitPlugin::class.java))

val scope: Scope = KTP.openScope(ThePitPlugin::class.java)