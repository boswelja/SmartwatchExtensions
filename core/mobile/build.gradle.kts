plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("app.cash.sqldelight")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.boswelja.smartwatchextensions.core"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    buildFeatures.buildConfig = true
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(projects.core.common)
    api(projects.playServicesWearableExt)

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
    databases {
        create("WatchSettingsDatabase") {
            packageName.set("com.boswelja.smartwatchextensions.core.settings.database")
            srcDirs("src/main/database-settings")
        }
        create("RegisteredWatchDatabase") {
            packageName.set("com.boswelja.smartwatchextensions.core.devicemanagement.database")
            srcDirs("src/main/database-watches")
        }
    }
}
