import de.comahe.i18n4k.gradle.plugin.i18n4k
import kr.entree.spigradle.kotlin.spigotAll

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    id("kr.entree.spigradle") version "1.2.4"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.flywaydb.flyway") version "9.18.0"
    id("de.comahe.i18n4k") version "0.5.0"
}

group = "me.abhigya"
version = "1.0-SNAPSHOT"

val javaVersion = 1.8
val toothpick_version = "3.1.0"
val adventure_version = "4.13.1"
val adventure_platform_version = "4.3.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("com.github.stephanenicolas.toothpick:ktp:$toothpick_version")
    kapt("com.github.stephanenicolas.toothpick:toothpick-compiler:$toothpick_version")
    compileOnly(spigotAll("1.16.5"))
//    compileOnly(fileTree(mapOf("dir" to "${rootProject.rootDir}/lib", "include" to listOf("*.jar"))))
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.8.0")
    implementation("com.github.cryptomorin:XSeries:9.1.0")
    implementation("com.jonahseguin:drink:1.0.5")
    implementation("fr.minuskube.inv:smart-invs:1.2.7") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.spongepowered:configurate-extra-kotlin:4.1.2")
    implementation("xyz.xenondevs:particle:1.7.1")
    implementation("org.flywaydb:flyway-core:9.18.0")
//    implementation("org.jooq:jooq:3.16.19")
//    implementation("org.jooq:jooq-kotlin:3.16.19")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("net.kyori:adventure-api:$adventure_version")
    implementation("net.kyori:adventure-text-serializer-gson:$adventure_version")
    implementation("net.kyori:adventure-text-serializer-legacy:$adventure_version")
    implementation("net.kyori:adventure-text-minimessage:$adventure_version")
    implementation("net.kyori:adventure-extra-kotlin:$adventure_version")
    implementation("net.kyori:adventure-platform-bukkit:$adventure_platform_version")
    implementation("de.comahe.i18n4k:i18n4k-core-jvm:0.5.0")
}

val databaseFile = "${rootProject.projectDir.absolutePath}\\database.db"

flyway {
    url = "jdbc:h2:$databaseFile"
    schemas = arrayOf("pit")
    defaultSchema = "pit"
}

i18n4k {
    sourceCodeLocales = listOf("en")
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
//        classpath("org.jooq:jooq-meta:3.16.19")
//        classpath("org.jooq:jooq-codegen:3.16.19")
        classpath("com.h2database:h2:2.1.214")
    }
}

configurations.implementation {
    exclude(group = "org.apache", module = "commons-lang")
    exclude(group = "org.apache", module = "commons-lang3")
    exclude(group = "commons-lang", module = "commons-lang")
}

tasks {
//    register("jooqGen") {
//        GenerationTool.generate(Configuration()
//            .withJdbc(Jdbc()
//                .withDriver("org.h2.Driver")
//                .withUrl("jdbc:h2:~/database.db"))
//            .withGenerator(Generator()
//                .withName(KotlinGenerator::class.java.canonicalName)
//                .withDatabase(Database()
//                    .withName(H2Database::class.java.canonicalName)
//                    .withInputSchema("pit")
//                    .withExcludes("flyway_schema_history"))
//                .withGenerate(Generate()
//                    .withJavadoc(true)
//                    .withComments(true)
//                    .withPojosAsKotlinDataClasses(true)
//                    .withDaos(true))
//                .withTarget(Target()
//                    .withPackageName("me.abhigya.jooq.codegen")
//                    .withDirectory("src/main/kotlin"))))
//        dependsOn(flywayMigrate)
//    }

    assemble {
        dependsOn(shadowJar)
    }

    compileKotlin {
        kotlinOptions.suppressWarnings = true
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        dependsOn(flywayMigrate)
//        dependsOn(getTasksByName("jooqGen", false))
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    shadowJar {
        val relocatePath = "me.abhigya.pit.libs"
        relocate("com.jonahseguin.drink", "$relocatePath.drink")
        relocate("toothpick", "$relocatePath.toothpick")

        exclude("com/google/errorprone/annotations/**")
        exclude("jakarta/xml/bind/**")
        exclude("DebugProbesKt.bin")
        exclude("META-INF/**")

        dependencies {
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("org.jetbrains.kotlinx:.*:.*"))
            exclude(dependency("org.jetbrains:annotations:.*"))
        }

        minimize()
    }

    jar {
        enabled = false
    }
}

kapt.includeCompileClasspath = false
flyway.cleanDisabled = false

spigot {
    name = "Pit"
    version = "1.0"
    authors = listOf("Abhigya")
    description = "Pit plugin"
}