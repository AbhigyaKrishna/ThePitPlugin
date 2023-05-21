package me.abhigya.pit.util

import org.bukkit.Bukkit

/**
 * An enumeration for most server versions, that implements some methods for comparing versions.
 */
enum class Version(
    /**
     * Gets the version's id.
     * @return Version's id
     */
    private val id: Int
) {
    /* legacy versions */
    V1_8_R1(181),
    V1_8_R2(182),
    V1_8_R3(183),
    V1_9_R1(191),
    V1_9_R2(192),
    V1_10_R1(1101),
    V1_11_R1(1111),
    V1_12_R1(1121),

    /* latest versions */
    V1_13_R1(1131),
    V1_13_R2(1132),
    V1_14_R1(1141),
    V1_15_R1(1151),
    V1_16_R1(1161),
    V1_16_R2(1162),
    V1_16_R3(1163),
    V1_17_R1(1171),
    V1_18_R1(1181),
    V1_19_R1(1191);

    val nmsPackage: String by lazy {
        if (SERVER_VERSION.isNewerEquals(V1_17_R1))
            "net.minecraft"
        else
            String.format(
                NMS_CLASSES_PACKAGE,
                SERVER_VERSION.name
            )
    }
    val obcPackage: String by lazy {
        String.format(CRAFT_CLASSES_PACKAGE, SERVER_VERSION.name)
    }

    /**
     * Checks whether this version is older than the provided version.
     *
     * @param other Other version
     * @return true if older
     */
    fun isOlder(other: Version): Boolean {
        return id < other.id
    }

    /**
     * Checks whether this version is older than or equals to the provided version.
     *
     * @param other Other version
     * @return true if older or equals
     */
    fun isOlderEquals(other: Version): Boolean {
        return id <= other.id
    }

    /**
     * Checks whether this version is newer than the provided version.
     *
     * @param other Other version
     * @return true if newer
     */
    fun isNewer(other: Version): Boolean {
        return id > other.id
    }

    /**
     * Checks whether this version is newer than or equals to the provided version.
     *
     * @param other Other version
     * @return true if newer or equals
     */
    fun isNewerEquals(other: Version): Boolean {
        return id >= other.id
    }

    /**
     * Checks whether this has the same version as the provided version.
     *
     * <pre>`
     * Version.1_8_R1.equalsVersion (1_8_R3) = true
     * Version.1_9_R1.equalsVersion (1_8_R1) = false
    `</pre> *
     *
     * @param other Other version
     * @return true if has the same version
     */
    fun equalsVersion(other: Version): Boolean {
        val s0 = name.substring(0, name.indexOf("_R"))
        val s1 = other.name.substring(0, other.name.indexOf("_R"))
        return s0 == s1
    }

    /**
     * Checks whether this has the same revision as the provided version.
     *
     * <pre>`
     * Version.1_8_R3.equalsRevision (1_9_R3) = true
     * Version.1_8_R1.equalsRevision (1_8_R3) = false
    `</pre> *
     *
     * @param other Other version
     * @return true if has the same revision
     */
    fun equalsRevision(other: Version): Boolean {
        val s0 = name.substring(name.indexOf("R") + 1)
        val s1 = other.name.substring(other.name.indexOf("R") + 1)
        return s0 == s1
    }

    companion object {
        const val CRAFT_CLASSES_PACKAGE = "org.bukkit.craftbukkit.%s"
        const val NMS_CLASSES_PACKAGE = "net.minecraft.server.%s"
        /**
         * Gets the version of the current running server.
         *
         * Note that server versions older than [Version.V1_8_R1] are NOT supported.
         *
         * @return Version of this server
         */
        val SERVER_VERSION: Version by lazy {
            val packaje = Bukkit.getServer().javaClass.getPackage().name
            val version = packaje.substring(packaje.lastIndexOf(".") + 1)
            valueOf(version)
        }
    }
}
