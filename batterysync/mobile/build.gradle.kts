plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("com.squareup.sqldelight")
}

android {
    namespace = "com.boswelja.smartwatchextensions.batterysync"
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
    api(projects.batterysync.common)

    implementation(projects.core.mobile)

    implementation(libs.watchconnection.mobile.core)

    implementation(libs.androidx.datastore.proto)

    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.android)
    implementation(libs.androidx.work)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.compose.mobile)
    implementation(libs.glance.appwidget)
    implementation(libs.appwidget.configuration)

    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlitedriver)

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

sqldelight {
    database("BatteryStatsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.batterysync.database"
    }
}
