// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    kotlin("android")
    id("com.android.library")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.squareup.sqldelight")
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
    api(libs.watchconnection.common)
    api(projects.appmanager.common)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.koin.core)
    implementation(libs.watchconnection.mobile.core)
    implementation(projects.core.mobile)
    implementation(libs.sqldelight.android)
    implementation(libs.androidx.work.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.compose.mobile)

    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlitedriver)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.corektx)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.work.test)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
    // Workaround for MockK 1.11.0 including a broken objenesis
    androidTestImplementation("org.objenesis:objenesis:3.2")
}

sqldelight {
    database("WatchAppDatabase") {
        packageName = "com.boswelja.smartwatchextensions.appmanager.database"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
