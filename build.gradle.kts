plugins {
    kotlin("jvm") version "1.5.10"
	id("application")
	id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
	mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Old
    val dropwizardVersion = "1.0.2"

	implementation("io.dropwizard:dropwizard-core:$dropwizardVersion")
	implementation("io.dropwizard:dropwizard-metrics:$dropwizardVersion")
	implementation("org.jsoup:jsoup:1.9.2")
	implementation("ru.vyarus:dropwizard-guicey:4.0.0")
	implementation("net.anthavio:airbrake-logback:1.0.3")

	testImplementation("junit:junit:4.12")
	testImplementation("nl.jqno.equalsverifier:equalsverifier:2.1.6")
	testImplementation("io.dropwizard:dropwizard-testing:$dropwizardVersion")
	testImplementation("io.dropwizard:dropwizard-client:$dropwizardVersion")
	testImplementation("com.squareup.retrofit2:converter-jackson:2.1.0")

    // New
    val ktorVersion = "1.6.0"
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
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
