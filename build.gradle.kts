buildscript {
    val kotlinVersion = "1.5.31"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0-alpha02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.squareup.wire:wire-gradle-plugin:${libs.versions.wire.get()}")
        classpath("com.squareup.sqldelight:gradle-plugin:${libs.versions.sqldelight.get()}")

    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    repositories {
        google()
        mavenCentral()
    }
    detekt {
        config = files("$rootDir/config/detekt/detekt.yml")
        source = files("src")
        buildUponDefaultConfig = true
        parallel = true
    }
}
