plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("app.cash.sqldelight")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.boswelja.smartwatchextensions.appmanager"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(projects.appmanager.common)

    api(libs.watchconnection.common)
    implementation(libs.watchconnection.mobile.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutines)
    implementation(projects.core.mobile)
    implementation(libs.sqldelight.android)
    implementation(libs.androidx.work)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.compose.mobile)

    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlitedriver)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
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
    databases {
        create("WatchAppDatabase") {
            packageName.set("com.boswelja.smartwatchextensions.appmanager.database")
        }
    }
}
