plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "io.jwharm.javagi.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.github.jwharm.javagi:gtk:0.11.1")
    implementation("io.github.jwharm.javagi:adw:0.11.1")
}

tasks.named<JavaExec>("run") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

application {
    mainClass.set("io.jwharm.javagi.examples.MainKt")
}

kotlin {
    jvmToolchain(22)
}
