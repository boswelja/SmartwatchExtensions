plugins {
    kotlin("multiplatform")
    id("com.boswelja.smartwatchextensions.library")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                api(libs.watchconnection.common)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.datastore.proto)
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
    }
}
