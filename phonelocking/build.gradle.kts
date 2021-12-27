plugins {
    id("multiplatform-module")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
            }
        }
        val androidMain by getting
    }
}
