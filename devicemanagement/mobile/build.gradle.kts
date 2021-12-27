plugins {
    id("com.boswelja.smartwatchextensions.library")
    kotlin("plugin.serialization") version "1.6.10"
    id("com.squareup.sqldelight")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.watchconnection.platform.wearos)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                api(projects.devicemanagement.common)
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
                implementation(libs.androidx.datastore.proto)
                implementation(libs.koin.android)
            }
        }
    }
}

sqldelight {
    database("RegisteredWatchDatabase") {
        packageName = "com.boswelja.smartwatchextensions.devicemanagement.database"
    }
}
