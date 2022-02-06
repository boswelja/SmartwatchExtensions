// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.dndsync.common)
                implementation(projects.settings.mobile)
                implementation(libs.watchconnection.mobile.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.bundles.lifecycle)
            }
        }
    }
}
