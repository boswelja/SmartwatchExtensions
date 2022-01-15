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
                implementation(libs.batterystats)
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
        val androidAndroidTest by getting {
            dependencies {
                implementation(libs.androidx.test.corektx)
                implementation(libs.androidx.test.runner)
                implementation(libs.mockk.android)
                implementation(libs.kotlinx.coroutines.test)
                // Workaround for MockK 1.11.0 including a broken objenesis
                implementation("org.objenesis:objenesis:3.2")
            }
        }
    }
}
