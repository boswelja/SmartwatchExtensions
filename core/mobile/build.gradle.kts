// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.squareup.sqldelight")
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.common)
                api(libs.watchconnection.common)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
                implementation(libs.bundles.compose.mobile)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlitedriver)
            }
        }
    }
}

sqldelight {
    database("WatchSettingsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.settings.database"
    }
}
