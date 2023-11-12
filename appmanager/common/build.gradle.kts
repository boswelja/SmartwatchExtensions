plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.boswelja.smartwatchextensions.appmanager.common"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(libs.watchconnection.common)
    implementation(libs.kotlinx.serialization.protobuf)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
