// Suppress DSL_SCOPE_VIOLATION for https://youtrack.jetbrains.com/issue/KTIJ-19369
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.squareup.sqldelight")
    alias(libs.plugins.compose)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                implementation(libs.watchconnection.mobile.core)
                implementation(projects.core.mobile)
                implementation(projects.messages.mobile)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
                implementation(libs.androidx.work.ktx)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.bundles.lifecycle)
                implementation(libs.bundles.compose.mobile)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlitedriver)
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
                // Workaround for MockK 1.11.0 including a broken objenesis
                implementation("org.objenesis:objenesis:3.2")
            }
        }
    }
}

sqldelight {
    database("BatteryStatsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.batterysync.database"
    }
}
