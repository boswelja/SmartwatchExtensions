// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.proximity.common)
                implementation(projects.core.mobile)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.watchconnection.mobile.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.bundles.lifecycle)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.bundles.compose.mobile)
            }
        }
    }
}
