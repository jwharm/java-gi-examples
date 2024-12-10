plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.poach3r"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.github.jwharm.javagi:gtk:0.11.1-SNAPSHOT")
    implementation("io.github.jwharm.javagi:adw:0.11.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(22)
}