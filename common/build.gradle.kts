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
    val androidxCoreKtx = "1.5.0-beta02"
    val playServicesWearable = "17.0.0"

    implementation("androidx.core:core-ktx:$androidxCoreKtx")

    implementation("com.google.android.gms:play-services-wearable:$playServicesWearable")
}
