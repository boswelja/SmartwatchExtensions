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
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(libs.watchconnection.common)
    api(libs.watchconnection.serialization)
    implementation(libs.kotlinx.serialization.protobuf)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("junit:junit:4.13.2")
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
