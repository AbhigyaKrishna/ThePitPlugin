package me.abhigya.pit.util

import me.abhigya.pit.ThePitPlugin
import toothpick.Toothpick
import toothpick.ktp.KTP

fun Any.injectMembers() = Toothpick.inject(this, KTP.openScope(ThePitPlugin::class.java))