package me.abhigya.pit

import toothpick.configuration.Configuration
import javax.inject.Scope

fun getConfiguration(): Configuration {
    return if (System.getProperty("environment") == "development" ||
        System.getProperty("toothpick.configuration") == "development") {
        Configuration.forDevelopment()
    } else {
        Configuration.forProduction()
    }
}

@Scope
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PitPluginScope

@Scope
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArenaScope