plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version "1.6.20"
}

android {
    namespace = "com.boswelja.smartwatchextensions.core.common"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(libs.watchconnection.common)
    api(libs.watchconnection.serialization)

    implementation(libs.kotlinx.serialization.protobuf)

    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.kotlinx.coroutines.test)
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
