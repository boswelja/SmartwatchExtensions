plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = PackageInfo.targetSdk

    defaultConfig {
        minSdk = 23
        targetSdk = PackageInfo.targetSdk
        consumerProguardFile("proguard-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}

dependencies {
    val androidxCoreKtx = "1.6.0-alpha01"

    implementation("androidx.core:core-ktx:$androidxCoreKtx")
}
