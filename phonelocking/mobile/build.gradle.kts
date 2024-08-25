plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.boswelja.smartwatchextensions.phonelocking"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.phonelocking.common)

    implementation(projects.core.mobile)

    api(libs.watchconnection.common)
    implementation(libs.watchconnection.mobile.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.bundles.compose.mobile)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
