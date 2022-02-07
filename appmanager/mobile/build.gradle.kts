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
                api(libs.watchconnection.common)
                api(projects.appmanager.common)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
                implementation(libs.watchconnection.mobile.core)
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
                implementation(projects.core.mobile)
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
                implementation(libs.mockk.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidAndroidTest by getting {
            dependencies {
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
    database("WatchAppDatabase") {
        packageName = "com.boswelja.smartwatchextensions.appmanager.database"
    }
}
