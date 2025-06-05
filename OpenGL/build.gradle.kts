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
    implementation("io.github.jwharm.javagi:gtk:0.11.2")
    implementation("io.github.jwharm.javagi:adw:0.11.2")
    implementation("org.lwjgl:lwjgl:3.3.6")
    runtimeOnly("org.lwjgl:lwjgl:3.3.6:natives-linux")
    implementation("org.lwjgl:lwjgl-opengl:3.3.6")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.6:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.6:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.6:natives-macos")
    implementation("org.lwjgl:lwjgl-opengles:3.3.6")
    runtimeOnly("org.lwjgl:lwjgl-opengles:3.3.6:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-opengles:3.3.6:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengles:3.3.6:natives-macos")
    implementation("org.joml:joml:1.10.8")
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