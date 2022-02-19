import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.10"

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
    implementation(kotlin("stdlib"))

    // Ktor
    implementation(platform("io.ktor:ktor-bom:1.6.7"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-serialization")
    implementation("io.ktor:ktor-client-okhttp")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("ch.qos.logback:logback-classic:1.2.10")

    implementation("com.bugsnag:bugsnag:3.6.3")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:5.1.0")
    testImplementation("io.strikt:strikt-jvm:0.33.0")
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

task("stage") {
    description = "Prepares the application for deployment"
    group = "build"
    dependsOn("clean", "shadowJar")
}

tasks.shadowJar.configure {
    mustRunAfter("clean")
}

defaultTasks "stage"
