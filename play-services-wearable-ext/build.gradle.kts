plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.wearableinterface"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.play.services.wearable)
    api(libs.kotlinx.coroutines.playservices)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
