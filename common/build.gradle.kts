plugins {
    id(BuildPlugins.androidLibrary)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinAndroidExtensions)
    id("com.diffplug.spotless") version "5.1.0"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(Libraries.kotlinStdlib)

    implementation(Libraries.androidxAppCompat)
    implementation(Libraries.androidxConstraintLayout)
    implementation(Libraries.androidxCoreKtx)
    implementation(Libraries.androidxPreference)

    implementation(Libraries.materialComponents)
    implementation(Libraries.playServicesWearable)
    implementation(Libraries.timber)
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
