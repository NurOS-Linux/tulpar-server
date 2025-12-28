import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.0"
    application
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "meigo.tulpar.server"
version = "2.0.0"

val ktorVersion = "3.3.3"
val cliktVersion = "5.0.3"
val mordantVersion = "3.0.2"
val hopliteVersion = "2.8.0"
val logbackVersion = "1.5.23"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // CLI (Clikt)
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("com.github.ajalt.clikt:clikt-markdown:$cliktVersion")

    // UI & ANSI (Mordant)
    implementation("com.github.ajalt.mordant:mordant:$mordantVersion")
    implementation("com.github.ajalt.mordant:mordant-coroutines:$mordantVersion")

    // Config (Hoplite + HOCON)
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("meigo.tulpar.server.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    mergeServiceFiles()
}

tasks.build {
    dependsOn("shadowJar")
}