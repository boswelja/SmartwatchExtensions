@file:Suppress("SpellCheckingInspection")

const val kotlinVersion = "1.4.0-rc"

object BuildPlugins {
    object Versions {
        const val buildToolsVersion = "4.2.0-alpha07"
        const val googleServicesVersion = "4.3.3"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val googleServicesPlugin = "com.google.gms:google-services:${Versions.googleServicesVersion}"

    const val androidLibrary = "com.android.library"
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"
}

object AndroidSdk {
    const val mobileMin = 21
    const val wearMin = 25
    const val compile = 30
    const val target = compile
}

object Libraries {
    object Versions {
        const val androidxAppCompat = "1.3.0-alpha01"
        const val androidxBrowser = "1.3.0-alpha04"
        const val androidxConstraintLayout = "2.0.0-beta8"
        const val androidxCoreKtx = "1.4.0-alpha01"
        const val androidxFragment = "1.3.0-alpha06"
        const val androidxLifecycle = "2.3.0-alpha05"
        const val androidxNavigation = "2.3.0"
        const val androidxPreference = "1.1.1"
        const val androidxRoom = "2.3.0-alpha01"
        const val androidxWear = "1.1.0-rc02"
        const val androidxWork = "2.4.0-rc01"

        const val billingClient = "3.0.0"

        const val googleMaterial = "1.3.0-alpha01"

        const val playServicesWearable = "17.0.0"
        const val wearableSupport = "2.7.0"

        const val timber = "4.7.1"
    }

    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"

    const val androidxAppCompat = "androidx.appcompat:appcompat:${Versions.androidxAppCompat}"
    const val androidxBrowser = "androidx.browser:browser:${Versions.androidxBrowser}"
    const val androidxConstraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.androidxConstraintLayout}"
    const val androidxCoreKtx = "androidx.core:core-ktx:${Versions.androidxCoreKtx}"
    const val androidxFragment = "androidx.fragment:fragment-ktx:${Versions.androidxFragment}"
    const val androidxLifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidxLifecycle}"
    const val androidxLifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.androidxLifecycle}"
    const val androidxLifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.androidxLifecycle}"
    const val androidxNavigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.androidxNavigation}"
    const val androidxNavigationUI = "androidx.navigation:navigation-ui-ktx:${Versions.androidxNavigation}"
    const val androidxPreference = "androidx.preference:preference:${Versions.androidxPreference}"
    const val androidxRoomRuntime = "androidx.room:room-runtime:${Versions.androidxRoom}"
    const val androidxRoomKtx = "androidx.room:room-ktx:${Versions.androidxRoom}"
    const val androidxRoomKapt = "androidx.room:room-compiler:${Versions.androidxRoom}"
    const val androidxWear = "androidx.wear:wear:${Versions.androidxWear}"
    const val androidxWork = "androidx.work:work-runtime-ktx:${Versions.androidxWork}"

    const val billingClient = "com.android.billingclient:billing:${Versions.billingClient}"

    const val materialComponents = "com.google.android.material:material:${Versions.googleMaterial}"

    const val playServicesWearable = "com.google.android.gms:play-services-wearable:${Versions.playServicesWearable}"
    const val wearableSupport = "com.google.android.support:wearable:${Versions.wearableSupport}"
    const val wearableCompile = "com.google.android.wearable:wearable:${Versions.wearableSupport}"

    const val timber = "com.jakewharton.timber:timber:4.7.1"
}

object DebugLibraries {
    object Versions {
        const val androidxFragment = Libraries.Versions.androidxFragment
    }

    const val androidxFragment = "androidx.fragment:fragment-testing:${Versions.androidxFragment}"
}

object TestLibraries {
    object Versions {
        const val androidxArch = "2.1.0"

        const val junit = "4.13"
        const val truth = "1.0.1"
    }

    const val androidxArch = "androidx.arch.core:core-testing:${Versions.androidxArch}"
    const val junit = "junit:junit:${Versions.junit}"
    const val googleTruth = "com.google.truth:truth:${Versions.truth}"
}

object AndroidTestLibraries {
    object Versions {
        const val androidxArch = TestLibraries.Versions.androidxArch
        const val androidxEspresso = "3.1.0"
        const val androidxRoom = Libraries.Versions.androidxRoom
        const val androidxNavigation = Libraries.Versions.androidxNavigation
        const val androidxTest = "1.3.0-rc01"
        const val androidxWork = Libraries.Versions.androidxWork

        const val truth = TestLibraries.Versions.truth
    }

    const val androidxArch = "androidx.arch.core:core-testing:${Versions.androidxArch}"
    const val androidxEspresso = "androidx.test.espresso:espresso-core:${Versions.androidxEspresso}"
    const val androidxRoom = "androidx.room:room-testing:${Versions.androidxRoom}"
    const val androidxNavigation = "androidx.navigation:navigation-testing:${Versions.androidxNavigation}"
    const val androidxTestRunner = "androidx.test:runner:${Versions.androidxTest}"
    const val androidxTestRules = "androidx.test:rules:${Versions.androidxTest}"
    const val androidxWork = "androidx.work:work-testing:${Versions.androidxWork}"

    const val googleTruth = "com.google.truth:truth:${Versions.truth}"
}