plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    kotlin("plugin.serialization") version "1.6.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.proximity.common)
                implementation(projects.core.wear)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.watchconnection.wear)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.bundles.lifecycle)
                implementation(libs.androidx.datastore.proto)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.koin.android)
            }
        }
    }
}
