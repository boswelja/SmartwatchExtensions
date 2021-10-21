buildscript {
    val kotlinVersion = "1.5.31"

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0-alpha02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.squareup.wire:wire-gradle-plugin:${libs.versions.wire.get()}")
        classpath("com.squareup.sqldelight:gradle-plugin:${libs.versions.sqldelight.get()}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
