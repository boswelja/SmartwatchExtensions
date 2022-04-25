plugins {
    kotlin("android")
    id("com.android.library")
    id("com.boswelja.smartwatchextensions.detekt")
}

android {
    compileSdk = 32
    defaultConfig {
        minSdk = 26
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(libs.watchconnection.common)
    api(libs.watchconnection.serialization)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
