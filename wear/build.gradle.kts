plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(PackageInfo.targetSdk)

    defaultConfig {
        minSdkVersion(25)
        targetSdkVersion(PackageInfo.targetSdk)

        applicationId = PackageInfo.packageName
        versionCode = PackageInfo.getVersionCode()
        versionName = PackageInfo.versionName

        testApplicationId = PackageInfo.packageName + ".test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix(DebugInfo.idSuffix)
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":common"))

    val androidxArch = "2.1.0"
    val androidxAppCompat = "1.3.0-alpha02"
    val androidxConstraintLayout = "2.1.0-alpha1"
    val androidxCoreKtx = "1.5.0-alpha05"
    val androidxEspresso = "3.1.0"
    val androidxFragment = "1.3.0-beta02"
    val androidxLifecycle = "2.3.0-beta01"
    val androidxNavigation = "2.3.2"
    val androidxPreference = "1.1.1"
    val androidxTest = "1.3.1-alpha02"
    val androidxTestExt = "1.1.2-rc03"
    val androidxWear = "1.2.0-alpha05"

    val googleMaterial = "1.3.0-alpha04"
    val playServicesWearable = "17.0.0"
    val timber = "4.7.1"
    val junit = "4.13"
    val truth = "1.1"
    val mockk = "1.10.3"
    val robolectric = "4.4"
    val coroutines = "1.4.2-native-mt"
    val wearableSupport = "2.8.1"

    implementation("androidx.appcompat:appcompat:$androidxAppCompat")
    implementation("androidx.constraintlayout:constraintlayout:$androidxConstraintLayout")
    implementation("androidx.core:core-ktx:$androidxCoreKtx")
    implementation("androidx.lifecycle:lifecycle-common-java8:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycle")
    implementation("androidx.navigation:navigation-fragment-ktx:$androidxNavigation")
    implementation("androidx.navigation:navigation-ui-ktx:$androidxNavigation")
    implementation("androidx.preference:preference:$androidxPreference")
    implementation("androidx.wear:wear:$androidxWear")

    implementation("com.google.android.support:wearable:$wearableSupport")
    implementation("com.google.android.gms:play-services-wearable:$playServicesWearable")
    implementation("com.google.android.material:material:$googleMaterial")
    implementation("com.jakewharton.timber:timber:$timber")

    compileOnly("com.google.android.wearable:wearable:$wearableSupport")

    debugImplementation("androidx.fragment:fragment-testing:$androidxFragment")

    testImplementation("androidx.arch.core:core-testing:$androidxArch")
    testImplementation("androidx.test:core-ktx:$androidxTest")
    testImplementation("androidx.test.ext:junit-ktx:$androidxTestExt")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines")
    testImplementation("junit:junit:$junit")
    testImplementation("com.google.truth:truth:$truth")
    testImplementation("io.mockk:mockk:$mockk")
    testImplementation("org.robolectric:robolectric:$robolectric")

    androidTestImplementation("androidx.test.espresso:espresso-core:$androidxEspresso")
    androidTestImplementation("androidx.navigation:navigation-testing:$androidxNavigation")
    androidTestImplementation("androidx.test:runner:$androidxTest")
    androidTestImplementation("androidx.test:rules:$androidxTest")
    androidTestImplementation("androidx.test:core-ktx:$androidxTest")
    androidTestImplementation("com.google.truth:truth:$truth")
    androidTestImplementation("io.mockk:mockk-android:$mockk")
}
