package me.abhigya.pit.util

enum class Platform {

    BUKKIT,
    SPIGOTMC,
    PAPER
    ;

    companion object {
        val CURRENT: Platform = runCatching {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            PAPER
        }.recoverCatching {
            Class.forName("org.spigotmc.AsyncCatcher")
            SPIGOTMC
        }.getOrElse {
            BUKKIT
        }
    }

}