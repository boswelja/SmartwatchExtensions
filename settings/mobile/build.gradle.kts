plugins {
    id("multiplatform-module")
    id("com.squareup.sqldelight")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(projects.settings.common)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlitedriver)
            }
        }
    }
}

sqldelight {
    database("WatchSettingsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.settings.database"
    }
}
