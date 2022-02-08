plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.proximity.common)
                implementation(projects.core.wear)
                implementation(libs.watchconnection.wear)
            }
        }
        val androidMain by getting
    }
}
