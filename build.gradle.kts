import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.0"
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {

    // New
    val ktorVersion = "1.6.0"
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.anthavio:airbrake-logback:1.0.3") // TODO Update version
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

application {
    mainClass.set("se.gustavkarlsson.skylight.Application")
}

task("stage") {
    description = "Prepares the application for deployment"
    group = "build"
    dependsOn("clean", "shadowJar")
}

tasks.shadowJar.configure {
    mustRunAfter("clean")
}

defaultTasks "stage"
