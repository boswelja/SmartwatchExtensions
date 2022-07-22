plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version "1.6.20"
}

android {
    compileSdk = 32
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(projects.proximity.common)

    implementation(projects.core.wear)

    implementation(libs.watchconnection.wear)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.koin.android)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
