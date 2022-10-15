plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.boswelja.smartwatchextensions.phonelocking"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(projects.core.wear)
    implementation(projects.phonelocking.common)

    implementation(projects.playServicesWearableExt)

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
