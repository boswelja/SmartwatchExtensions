plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.safeArgs)
}

android {
    compileSdkVersion(AndroidSdk.compile)

    defaultConfig {
        minSdkVersion(AndroidSdk.wearMin)
        targetSdkVersion(AndroidSdk.target)

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

    implementation(Libraries.androidxAppCompat)
    implementation(Libraries.androidxConstraintLayout)
    implementation(Libraries.androidxCoreKtx)
    implementation(Libraries.androidxFragment)
    implementation(Libraries.androidxLifecycleCommon)
    implementation(Libraries.androidxLifecycleLiveData)
    implementation(Libraries.androidxLifecycleViewModel)
    implementation(Libraries.androidxNavigationUI)
    implementation(Libraries.androidxNavigationFragment)
    implementation(Libraries.androidxPreference)
    implementation(Libraries.androidxWear)

    implementation(Libraries.wearableSupport)
    implementation(Libraries.playServicesWearable)
    implementation(Libraries.materialComponents)

    implementation(Libraries.timber)

    compileOnly(Libraries.wearableCompile)

    debugImplementation(DebugLibraries.androidxFragment)

    testImplementation(TestLibraries.androidxArch)
    testImplementation(TestLibraries.androidxTestCore)
    testImplementation(TestLibraries.androidxTestExt)
    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.googleTruth)
    testImplementation(TestLibraries.mockk)
    testImplementation(TestLibraries.robolectric)

    androidTestImplementation(AndroidTestLibraries.googleTruth)
    androidTestImplementation(AndroidTestLibraries.androidxEspresso)
    androidTestImplementation(AndroidTestLibraries.androidxTestRunner)
    androidTestImplementation(AndroidTestLibraries.androidxTestRules)
    androidTestImplementation(AndroidTestLibraries.androidxTestExt)
    androidTestImplementation(AndroidTestLibraries.androidxTestCore)
    androidTestImplementation(AndroidTestLibraries.androidxNavigation)
}
