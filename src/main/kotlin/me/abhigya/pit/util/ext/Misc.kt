package me.abhigya.pit.util.ext

import java.util.logging.Logger

inline fun <reified T> logger(): Logger = Logger.getLogger(T::class.java.name)