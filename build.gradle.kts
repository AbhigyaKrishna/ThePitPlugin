import de.comahe.i18n4k.gradle.plugin.i18n4k
import org.jooq.codegen.GenerationTool
import org.jooq.codegen.KotlinGenerator
import org.jooq.meta.hsqldb.HSQLDBDatabase
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("org.flywaydb.flyway") version "9.18.0"
    id("de.comahe.i18n4k") version "0.5.0"
}

group = "me.abhigya"
version = "1.0-SNAPSHOT"

val javaVersion = 17
val toothpick_version = "3.1.0"
val adventure_version = "4.13.1"
val adventure_platform_version = "4.3.0"
val jooq_version = "3.18.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("com.github.stephanenicolas.toothpick:ktp:$toothpick_version")
    kapt("com.github.stephanenicolas.toothpick:toothpick-compiler:$toothpick_version")
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly(fileTree(mapOf("dir" to "${rootProject.rootDir}/lib", "include" to listOf("*.jar"))))
    implementation("com.github.cryptomorin:XSeries:9.1.0")
    implementation("fr.minuskube.inv:smart-invs:1.2.7")
    implementation("space.arim.dazzleconf:dazzleconf-ext-snakeyaml:1.2.0-M2")
    implementation("xyz.xenondevs:particle:1.7.1")
    implementation("org.flywaydb:flyway-core:9.18.0")
    implementation("org.jooq:jooq:$jooq_version")
    implementation("org.jooq:jooq-kotlin:$jooq_version")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("net.kyori:adventure-api:$adventure_version")
    implementation("net.kyori:adventure-text-serializer-gson:$adventure_version")
    implementation("net.kyori:adventure-text-serializer-legacy:$adventure_version")
    implementation("net.kyori:adventure-text-minimessage:$adventure_version")
    implementation("net.kyori:adventure-extra-kotlin:$adventure_version")
    implementation("net.kyori:adventure-platform-bukkit:$adventure_platform_version")
    implementation("de.comahe.i18n4k:i18n4k-core-jvm:0.5.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

val databaseUrl = "jdbc:hsqldb:file:${project.buildDir}/schema-gen/database;shutdown=true"

flyway {
    driver = "org.hsqldb.jdbc.JDBCDriver"
    user = "SA"
    password = ""
    url = databaseUrl
    validateMigrationNaming = true
    table = "schema_history"
    placeholders = mapOf(
        "table_prefix" to "",
        "uuidtype" to "UUID",
        "options" to ""
    )
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
        classpath("org.jooq:jooq-meta:3.18.0")
        classpath("org.jooq:jooq-codegen:3.18.0")
        classpath("org.hsqldb:hsqldb:2.5.2")
    }
}

configurations.implementation {
    exclude(group = "org.apache", module = "commons-lang")
    exclude(group = "org.apache", module = "commons-lang3")
    exclude(group = "commons-lang", module = "commons-lang")
}

tasks {
    register("jooqGen") {
        GenerationTool.generate(
            Configuration()
                .withJdbc(
                    Jdbc()
                        .withDriver("org.hsqldb.jdbc.JDBCDriver")
                        .withUrl(databaseUrl)
                        .withUser("SA")
                        .withPassword(""),
                )
                .withGenerator(
                    Generator()
                        .withName(KotlinGenerator::class.java.canonicalName)
                        .withDatabase(
                            Database()
                                .withName(HSQLDBDatabase::class.java.canonicalName)
                                .withInputSchema("PUBLIC")
                                .withIncludes(".*")
                                .withExcludes("flyway_schema_history")
                                .withExcludes("(?i:information_schema\\..*)|(?i:system_lobs\\..*)")
                                .withSchemaVersionProvider("SELECT :schema_name || '_' || MAX(\"version\") FROM \"schema_history\"")
                                .withForcedTypes(ForcedType()
                                    .withUserType("java.util.UUID")
                                    .withBinding("me.abhigya.pit.database.binding.UUIDBinding")
                                    .withIncludeExpression(".*\\.(UUID)\$")
                                    .withIncludeTypes("^UUID\$"),
                                    ForcedType()
                                        .withUserType("me.abhigya.pit.model.Balance")
                                        .withConverter("me.abhigya.pit.database.binding.BalanceConverter")
                                        .withIncludeExpression(".*\\.(BALANCE)\$")
                                        .withIncludeTypes("^FLOAT\$")
                                ),
                        )
                        .withGenerate(
                            Generate()
                                .withJavadoc(true)
                                .withComments(true)
                                .withDaos(true)
                        )
                        .withTarget(
                            Target()
                                .withPackageName("me.abhigya.jooq.codegen")
                                .withDirectory("src/main/kotlin"),
                        ),
                ),
        )
        dependsOn(flywayMigrate)
    }

    assemble {
        dependsOn(shadowJar)
    }

    compileKotlin {
        kotlinOptions.suppressWarnings = true
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        dependsOn(flywayMigrate)
        dependsOn(getTasksByName("jooqGen", false))
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

bukkit {
    main = "me.abhigya.pit.ThePitPlugin"
    name = "Pit"
    version = "1.0"
    authors = listOf("Abhigya")
    description = "Pit plugin"
    apiVersion = "1.13"
}