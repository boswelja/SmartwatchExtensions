// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    kotlin("multiplatform")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.boswelja.smartwatchextensions.library")
    kotlin("plugin.serialization") version "1.6.10"
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                implementation(projects.core.wear)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.watchconnection.wear)
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
                implementation(libs.androidx.datastore.proto)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.androidx.wear.complications.data.source)
                implementation(libs.bundles.compose.wear)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.androidx.wear.core)
            }
        }
        val androidAndroidTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.androidx.test.corektx)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.work.test)
                implementation(libs.koin.test)
                implementation(libs.mockk.android)
            }
        }
    }
}
