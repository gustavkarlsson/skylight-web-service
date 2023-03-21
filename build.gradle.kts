import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.RepositoryBuilder
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")
    }
}

plugins {
    val kotlinVersion = "1.7.20" // Match version set in dependencies

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val kotlinVersion = "1.7.20" // Match version set in plugins

    implementation(kotlin("stdlib:$kotlinVersion"))

    // Ktor
    implementation(platform("io.ktor:ktor-bom:2.1.2"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-cio")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-metrics-micrometer")

    implementation("io.ktor:ktor-client-cio")

    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("io.micrometer:micrometer-registry-prometheus:1.9.5")

    implementation("ch.qos.logback:logback-classic:1.4.3")

    implementation("com.rollbar:rollbar-java:1.10.0")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:5.5.0")
    testImplementation("io.strikt:strikt-jvm:0.34.1")
}

val generatedResourcesDir = file("$buildDir/generated-resources")

val generateGitCommitResource = tasks.register("generateGitCommitResource") {
    val head = getHeadRef()
    val commit = head.objectId.abbreviate(8).name()
    val file = file("$generatedResourcesDir/commit.txt")
    inputs.property("commit", commit)
    outputs.file(file)
    doLast {
        file.writeText(commit)
    }
}

fun getHeadRef(): Ref {
    val repo = RepositoryBuilder().run {
        gitDir = File(rootDir, "/.git")
        readEnvironment()
        build()
    }
    return repo.exactRef("HEAD")
}

sourceSets.main {
    resources.srcDir(generatedResourcesDir)
}

tasks.processResources {
    dependsOn(generateGitCommitResource)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

application {
    mainClass.set("se.gustavkarlsson.skylight.Application")
}

val stage = task("stage") {
    description = "Prepares the application for deployment"
    group = "build"
}

defaultTasks(stage)
