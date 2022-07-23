plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.proximity.common"
    compileSdk = 32
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
