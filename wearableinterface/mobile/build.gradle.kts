plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.wearableinterface"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.playservices)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
