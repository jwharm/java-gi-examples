plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "org.java-gi.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jwharm.cairobindings:cairo:1.18.4.1")
    implementation("org.java-gi:gtk:0.13.1")
}

kotlin {
    jvmToolchain(22)
}

application {
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    mainClass = "io.github.jwharm.javagi.examples.SpacewarKt"
}
