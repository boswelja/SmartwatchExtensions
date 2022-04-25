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
    implementation(projects.core.common)

    api(libs.watchconnection.common)
    api(libs.watchconnection.serialization)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.batterystats)
    implementation(libs.androidx.appcompat)

    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk.core)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
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
