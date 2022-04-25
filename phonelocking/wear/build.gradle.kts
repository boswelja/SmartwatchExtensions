// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    kotlin("android")
    id("com.android.library")
    id("com.boswelja.smartwatchextensions.detekt")
    kotlin("plugin.serialization") version "1.6.10"
    alias(libs.plugins.compose)
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
    api(projects.phonelocking.common)

    implementation(libs.watchconnection.wear)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.bundles.compose.wear)
    implementation(libs.androidx.wear.core)
    implementation(libs.androidx.wear.complications.data.source)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
