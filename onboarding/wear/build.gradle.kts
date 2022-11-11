plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.core"
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
    implementation(projects.onboarding.common)
    implementation(projects.playServicesWearableExt)

    implementation(libs.bundles.compose.wear)
    implementation(libs.koin.android)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
