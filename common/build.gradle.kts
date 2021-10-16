plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = PackageInfo.targetSdk

    defaultConfig {
        minSdk = 23
        targetSdk = PackageInfo.targetSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.watchconnection.common)
}
