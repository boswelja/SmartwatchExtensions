plugins {
    kotlin("multiplatform")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.boswelja.smartwatchextensions.library")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                implementation(libs.watchconnection.wear)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.datastore.proto)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.androidx.wear.complications.data.source)
            }
        }
    }
}
