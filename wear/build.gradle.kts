plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinAndroidExtensions)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.safeArgs)
    id("com.diffplug.spotless") version "5.1.0"
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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

    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.googleTruth)

    androidTestImplementation(AndroidTestLibraries.googleTruth)
    androidTestImplementation(AndroidTestLibraries.androidxEspresso)
    androidTestImplementation(AndroidTestLibraries.androidxTestRunner)
    androidTestImplementation(AndroidTestLibraries.androidxTestRules)
}

spotless {
    kotlin {
        target("**/*.kt")

        ktlint("0.37.2")
        endWithNewline()

        licenseHeaderFile("../License")
    }
    kotlinGradle {
        ktlint("0.37.2")
        endWithNewline()
    }
    format("xml") {
        target("**/*.xml")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}
