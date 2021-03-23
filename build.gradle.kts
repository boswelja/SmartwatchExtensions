buildscript {
    val kotlinVersion = "1.4.30"

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha11")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.5")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
