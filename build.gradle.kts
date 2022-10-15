import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.20" // Match version set in dependencies

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val kotlinVersion = "1.7.20" // Match version set in dependencies

    implementation(kotlin("stdlib:$kotlinVersion"))

    // Ktor
    implementation(platform("io.ktor:ktor-bom:2.1.2"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-okhttp")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    implementation("ch.qos.logback:logback-classic:1.4.3")

    implementation("com.bugsnag:bugsnag:3.6.4")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:5.5.0")
    testImplementation("io.strikt:strikt-jvm:0.34.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

application {
    mainClass.set("se.gustavkarlsson.skylight.Application")
}

val stage = task("stage") {
    description = "Prepares the application for deployment"
    group = "build"
}

defaultTasks(stage)
