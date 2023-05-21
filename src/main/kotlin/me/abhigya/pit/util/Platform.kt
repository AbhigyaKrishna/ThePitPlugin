package me.abhigya.pit.util

enum class Platform {

    BUKKIT,
    SPIGOTMC,
    PAPER
    ;

    companion object {
        val CURRENT: Platform = try {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            PAPER
        } catch (ex: ClassNotFoundException) {
            try {
                Class.forName("org.spigotmc.AsyncCatcher")
                SPIGOTMC
            } catch (ex: ClassNotFoundException) {
                BUKKIT
            }
        }
    }

}