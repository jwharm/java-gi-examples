plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "io.jwharm.javagi.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jwharm.cairobindings:cairo:1.18.4")
    implementation("io.github.jwharm.javagi:gtk:0.10.2")
}

kotlin {
    jvmToolchain(22)
}

application {
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=/usr/lib64:/lib64:/lib:/usr/lib:/lib/x86_64-linux-gnu"
    )
    mainClass = "io.github.jwharm.javagi.examples.SpacewarKt"
}
