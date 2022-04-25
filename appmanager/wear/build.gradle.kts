plugins {
    kotlin("android")
    id("com.android.library")
    id("com.boswelja.smartwatchextensions.detekt")
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
    api(projects.appmanager.common)

    api(libs.watchconnection.common)
    implementation(libs.watchconnection.wear)

    implementation(libs.koin.core)
    implementation(libs.androidx.core.ktx)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.test.corektx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
}
