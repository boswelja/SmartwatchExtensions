plugins {
    kotlin("android")
    id("com.android.library")
    id("com.boswelja.smartwatchextensions.detekt")
    kotlin("plugin.serialization") version "1.6.10"
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
    api(projects.dndsync.common)

    implementation(projects.core.wear)

    implementation(libs.watchconnection.wear)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.koin.android)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
