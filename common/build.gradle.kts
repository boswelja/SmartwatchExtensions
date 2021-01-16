plugins {
    id(BuildPlugins.androidLibrary)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinKapt)
}

android {
    compileSdkVersion(AndroidSdk.compile)

    defaultConfig {
        minSdkVersion(AndroidSdk.mobileMin)
        targetSdkVersion(AndroidSdk.target)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    val androidxAppCompat = "1.3.0-alpha02"
    val androidxConstraintLayout = "2.1.0-alpha1"
    val androidxCoreKtx = "1.5.0-alpha05"
    val androidxPreference = "1.1.1"

    val googleMaterial = "1.3.0-alpha04"
    val playServicesWearable = "17.0.0"
    val timber = "4.7.1"

    implementation("androidx.appcompat:appcompat:$androidxAppCompat")
    implementation("androidx.constraintlayout:constraintlayout:$androidxConstraintLayout")
    implementation("androidx.core:core-ktx:$androidxCoreKtx")
    implementation("androidx.preference:preference:$androidxPreference")

    implementation("com.google.android.gms:play-services-wearable:$playServicesWearable")
    implementation("com.google.android.material:material:$googleMaterial")
    implementation("com.jakewharton.timber:timber:$timber")
}
