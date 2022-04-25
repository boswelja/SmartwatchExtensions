// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("com.squareup.sqldelight")
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
    api(projects.core.common)

    implementation(libs.watchconnection.mobile.core)

    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.android)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.bundles.compose.mobile)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.koin.android)

    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlitedriver)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}

sqldelight {
    database("WatchSettingsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.core.settings.database"
        sourceFolders = listOf("database-settings")
    }
    database("RegisteredWatchDatabase") {
        packageName = "com.boswelja.smartwatchextensions.core.devicemanagement.database"
        sourceFolders = listOf("database-watches")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
