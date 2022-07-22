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

    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

dependencies {
    api(projects.phonelocking.common)

    implementation(libs.watchconnection.wear)

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}
