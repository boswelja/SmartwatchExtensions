plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.dndsync.common"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime)
}

detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
