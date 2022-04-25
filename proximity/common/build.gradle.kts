plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = 32
    defaultConfig {
        minSdk = 26
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
