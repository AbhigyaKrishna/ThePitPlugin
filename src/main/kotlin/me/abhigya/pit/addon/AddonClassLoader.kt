package me.abhigya.pit.addon

import com.google.common.io.ByteStreams
import toothpick.Scope
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSource
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.jvm.Throws

class AddonClassLoader internal constructor(
    val parent: ClassLoader,
    val addonDescription: AddonDescription,
    val scope: Scope,
    val addonFile: File
) : URLClassLoader(arrayOf(addonFile.toURI().toURL()), parent) {

    companion object {
        init {
            ClassLoader.registerAsParallelCapable()
        }
    }

    private val classes = ConcurrentHashMap<String, Class<*>>()
    private val jarFile = JarFile(addonFile)
    private val manifest = jarFile.manifest
    private val url = addonFile.toURI().toURL()

    internal val addon: PitAddon

    private val classBlacklist = setOf(
        "org.bukkit.",
        "net.minecraft.",
        "kotlin.",
        "kotlinx.",
        "me.abhigya.pit."
    )

    init {
        try {
            val jarClass = try {
                Class.forName(addonDescription.main(), true, this)
            } catch (e: ClassNotFoundException) {
                throw InvalidAddonException("Cannot find main class '${addonDescription.main()}'")
            }

            val addonClass = try {
                jarClass.asSubclass(PitAddon::class.java)
            } catch (e: ClassCastException) {
                throw InvalidAddonException("Main class '${addonDescription.main()}' does not extend PitAddon")
            }

            addon = addonClass.getConstructor(Scope::class.java).newInstance(scope)
        } catch (e: IllegalAccessException) {
            throw InvalidAddonException("No public constructor", e)
        } catch (e: InstantiationException) {
            throw InvalidAddonException("Abnormal plugin type", e)
        }
    }

    override fun getResource(name: String): URL? {
        return findResource(name)
    }

    override fun getResources(name: String): Enumeration<URL> {
        return findResources(name)
    }

    @Throws(ClassNotFoundException::class)
    override fun findClass(moduleName: String, name: String): Class<*> {
        if (classBlacklist.any { name.startsWith(it) }) {
            throw ClassNotFoundException(name)
        }

        var result = classes[name]

        if (result == null) {
            val path = name.replace('.', '/') + ".class"
            val entry = jarFile.getJarEntry(path)

            if (entry != null) {
                val classBytes = try {
                    jarFile.getInputStream(entry).use { ByteStreams.toByteArray(it) }
                } catch (ex: IOException) {
                    throw ClassNotFoundException(name, ex)
                }
                val dot = name.lastIndexOf('.')
                if (dot != -1) {
                    val pkgName = name.substring(0, dot)
                    if (getDefinedPackage(pkgName) == null) {
                        try {
                            manifest?.let { definePackage(pkgName, it, url) } ?: definePackage(
                                pkgName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                            )
                        } catch (ex: IllegalArgumentException) {
                            checkNotNull(getDefinedPackage(pkgName)) { "Cannot find package $pkgName" }
                        }
                    }
                }

                val signers = entry.codeSigners
                val source = CodeSource(url, signers)
                result = defineClass(name, classBytes, 0, classBytes.size, source)
            }

            if (result == null) {
                result = super.findClass(name)
            }

            classes[name] = result!!
        }

        return result;
    }

    override fun close() {
        try {
            super.close()
        } finally {
            jarFile.close()
        }
    }


}

class InvalidAddonException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}