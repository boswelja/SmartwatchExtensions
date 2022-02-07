plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    kotlin("plugin.serialization") version "1.6.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.common)
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.datastore.proto)
                implementation(libs.koin.android)
            }
        }
    }
}
