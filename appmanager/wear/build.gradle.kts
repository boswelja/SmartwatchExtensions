plugins {
    kotlin("android")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions.appmanager"
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(projects.appmanager.common)

    implementation(projects.playServicesWearableExt)

    implementation(libs.koin.android)
    implementation(libs.androidx.core)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.mockk.android)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
