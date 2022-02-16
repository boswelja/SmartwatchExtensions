// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.squareup.sqldelight")
    alias(libs.plugins.compose)
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.messages.common)
                implementation(projects.core.mobile)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
                implementation(libs.bundles.compose.mobile)
                implementation(libs.koin.compose)
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
    database("MessageDatabase") {
        packageName = "com.boswelja.smartwatchextensions.messages.database"
    }
}
