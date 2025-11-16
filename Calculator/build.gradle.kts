plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "org.java-gi.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.java-gi:gtk:0.13.0")
    implementation("org.java-gi:adw:0.13.0")
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
