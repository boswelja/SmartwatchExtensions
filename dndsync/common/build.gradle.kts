plugins {
    id("com.boswelja.smartwatchextensions.library")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(libs.watchconnection.serialization)
                api(libs.kotlinx.coroutines.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.service)
                implementation(libs.androidx.lifecycle.runtime)
            }
        }
    }
}
