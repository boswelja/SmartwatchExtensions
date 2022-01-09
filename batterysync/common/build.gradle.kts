plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    kotlin("plugin.serialization") version "1.6.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(libs.watchconnection.serialization)
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.mockk.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
