plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

android {
    namespace = "com.boswelja.smartwatchextensions.core"
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
    api(projects.core.common)
    api(projects.playServicesWearableExt)

    api(libs.watchconnection.common)

    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.android)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.bundles.compose.mobile)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlitedriver)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}

sqldelight {
    database("WatchSettingsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.core.settings.database"
        sourceFolders = listOf("database-settings")
    }
    database("RegisteredWatchDatabase") {
        packageName = "com.boswelja.smartwatchextensions.core.devicemanagement.database"
        sourceFolders = listOf("database-watches")
    }
}
