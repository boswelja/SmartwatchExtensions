plugins {
    id("multiplatform-module")
    kotlin("plugin.serialization") version "1.6.10"
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
        val androidMain by getting
    }
}
