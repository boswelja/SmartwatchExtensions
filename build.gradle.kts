buildscript {
    val kotlinVersion = "1.4.30"

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha10")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.4")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
