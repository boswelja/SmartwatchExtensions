plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.boswelja.smartwatchextensions.phonelocking"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.core.wear)
    api(projects.phonelocking.common)

    implementation(projects.playServicesWearableExt)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.bundles.compose.wear)
    implementation(libs.androidx.wear.core)
    implementation(libs.androidx.wear.complications.data.source)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
