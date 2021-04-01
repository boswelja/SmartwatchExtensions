plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(PackageInfo.targetSdk)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(PackageInfo.targetSdk)
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
