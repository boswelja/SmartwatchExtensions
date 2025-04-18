plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.boswelja.smartwatchextensions.appmanager.common"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.serialization.protobuf)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
