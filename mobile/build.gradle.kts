plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinAndroidExtensions)
    id(BuildPlugins.kotlinKapt)
    id("com.diffplug.spotless") version "5.1.0"
}

android {
    compileSdkVersion(AndroidSdk.compile)

    defaultConfig {
        minSdkVersion(AndroidSdk.mobileMin)
        targetSdkVersion(AndroidSdk.target)

        applicationId = PackageInfo.packageName
        versionCode = PackageInfo.getVersionCode()
        versionName = PackageInfo.versionName

        testApplicationId = PackageInfo.packageName + ".test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("room.schemaLocation" to "$rootDir/schemas"))
            }
        }
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
    implementation(fileTree("libs") { include("*.aar") })
    implementation(project(":common"))

    implementation(Libraries.androidxAppCompat)
    implementation(Libraries.androidxBrowser)
    implementation(Libraries.androidxConstraintLayout)
    implementation(Libraries.androidxCoreKtx)
    implementation(Libraries.androidxFragment)
    implementation(Libraries.androidxLifecycleViewModel)
    implementation(Libraries.androidxLifecycleLiveData)
    implementation(Libraries.androidxLifecycleService)
    implementation(Libraries.androidxLifecycleCommon)
    implementation(Libraries.androidxNavigationFragment)
    implementation(Libraries.androidxNavigationUI)
    implementation(Libraries.androidxPreference)
    implementation(Libraries.androidxRoomRuntime)
    implementation(Libraries.androidxRoomKtx)
    implementation(Libraries.androidxWork)

    implementation(Libraries.billingClient)
    implementation(Libraries.materialComponents)
    implementation(Libraries.playServicesWearable)
    implementation(Libraries.timber)

    kapt(Libraries.androidxRoomKapt)

    debugImplementation(DebugLibraries.androidxFragment)

    testImplementation(TestLibraries.androidxArch)
    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.googleTruth)

    androidTestImplementation(AndroidTestLibraries.androidxEspresso)
    androidTestImplementation(AndroidTestLibraries.androidxTestRunner)
    androidTestImplementation(AndroidTestLibraries.androidxTestRules)
    androidTestImplementation(AndroidTestLibraries.googleTruth)
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
