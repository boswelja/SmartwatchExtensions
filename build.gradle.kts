buildscript {
    val kotlinVersion = "1.5.10"

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-alpha05")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("com.squareup.wire:wire-gradle-plugin:3.7.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
