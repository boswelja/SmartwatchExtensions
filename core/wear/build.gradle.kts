plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.boswelja.smartwatchextensions.core"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(projects.core.common)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.koin.android)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
