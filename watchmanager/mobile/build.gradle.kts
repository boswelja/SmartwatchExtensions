plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.boswelja.smartwatchextensions.watchmanager"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.core.mobile)
    implementation(projects.watchmanager.common)
    implementation(projects.playServicesWearableExt)

    implementation(libs.bundles.compose.mobile)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
