plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.watchmanager"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.watchmanager.common)
    implementation(projects.playServicesWearableExt)

    implementation(libs.androidx.core)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
