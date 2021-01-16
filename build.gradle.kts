buildscript {
    val kotlinVersion = "1.4.21"

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha04")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.4")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.2")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
