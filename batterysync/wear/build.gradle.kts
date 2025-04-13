plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.boswelja.smartwatchextensions.batterysync"
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
    api(projects.batterysync.common)
    implementation(projects.playServicesWearableExt)

    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.androidx.wear.complications.data.source)
    implementation(libs.bundles.compose.wear)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.androidx.wear.core)

    testImplementation(libs.junit)

    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.work.test)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
