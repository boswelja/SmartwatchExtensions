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
    api(libs.watchconnection.common)
    api(projects.appmanager.common)
    implementation(libs.watchconnection.wear)
    implementation(libs.koin.core)
    implementation(libs.androidx.core.ktx)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.test.corektx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
    // Workaround for MockK 1.11.0 including a broken objenesis
    androidTestImplementation("org.objenesis:objenesis:3.2")
}
