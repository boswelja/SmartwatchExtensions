plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.dndsync.common"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(libs.watchconnection.common)
    api(libs.watchconnection.serialization)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime)
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
