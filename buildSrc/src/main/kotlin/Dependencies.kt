@file:Suppress("SpellCheckingInspection")

const val kotlinVersion = "1.4.21"

object BuildPlugins {
    object Versions {
        const val buildToolsVersion = "7.0.0-alpha03"
        const val googleServicesVersion = "4.3.3"
        const val androidxNavigation = Libraries.Versions.androidxNavigation
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val googleServicesPlugin =
        "com.google.gms:google-services:${Versions.googleServicesVersion}"
    const val navSafeArgsPlugin =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.androidxNavigation}"

    const val androidLibrary = "com.android.library"
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinAndroidExtensions = "kotlin-android-extensions"
    const val kotlinKapt = "kotlin-kapt"
    const val safeArgs = "androidx.navigation.safeargs.kotlin"
}

object AndroidSdk {
    const val mobileMin = 23
    const val wearMin = 25
    const val compile = 30
    const val target = compile
}

object Libraries {
    object Versions {
        const val androidxAppCompat = "1.3.0-alpha02"
        const val androidxBrowser = "1.3.0"
        const val androidxConstraintLayout = "2.1.0-alpha1"
        const val androidxCoreKtx = "1.5.0-alpha05"
        const val androidxFragment = "1.3.0-beta02"
        const val androidxLifecycle = "2.3.0-beta01"
        const val androidxNavigation = "2.3.2"
        const val androidxPaging = "3.0.0-alpha10"
        const val androidxPreference = "1.1.1"
        const val androidxRoom = "2.3.0-alpha03"
        const val androidxWear = "1.2.0-alpha03"
        const val androidxWork = "2.5.0-beta02"

        const val billingClient = "3.0.2"

        const val googleMaterial = "1.3.0-alpha04"
        const val googlePlayCore = "1.9.0"

        const val playServicesWearable = "17.0.0"
        const val wearableSupport = "2.8.1"

        const val timber = "4.7.1"
    }

    const val androidxAppCompat = "androidx.appcompat:appcompat:${Versions.androidxAppCompat}"
    const val androidxBrowser = "androidx.browser:browser:${Versions.androidxBrowser}"
    const val androidxConstraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.androidxConstraintLayout}"
    const val androidxCoreKtx = "androidx.core:core-ktx:${Versions.androidxCoreKtx}"
    const val androidxFragment = "androidx.fragment:fragment-ktx:${Versions.androidxFragment}"
    const val androidxLifecycleViewModel =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidxLifecycle}"
    const val androidxLifecycleLiveData =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.androidxLifecycle}"
    const val androidxLifecycleService =
        "androidx.lifecycle:lifecycle-service:${Versions.androidxLifecycle}"
    const val androidxLifecycleCommon =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.androidxLifecycle}"
    const val androidxNavigationFragment =
        "androidx.navigation:navigation-fragment-ktx:${Versions.androidxNavigation}"
    const val androidxNavigationUI =
        "androidx.navigation:navigation-ui-ktx:${Versions.androidxNavigation}"
    const val androidxPagingRuntime = "androidx.paging:paging-runtime:${Versions.androidxPaging}"
    const val androidxPreference = "androidx.preference:preference:${Versions.androidxPreference}"
    const val androidxRoomRuntime = "androidx.room:room-runtime:${Versions.androidxRoom}"
    const val androidxRoomKtx = "androidx.room:room-ktx:${Versions.androidxRoom}"
    const val androidxRoomKapt = "androidx.room:room-compiler:${Versions.androidxRoom}"
    const val androidxWear = "androidx.wear:wear:${Versions.androidxWear}"
    const val androidxWork = "androidx.work:work-runtime-ktx:${Versions.androidxWork}"

    const val billingClient = "com.android.billingclient:billing-ktx:${Versions.billingClient}"

    const val materialComponents = "com.google.android.material:material:${Versions.googleMaterial}"
    const val playCore = "com.google.android.play:core:${Versions.googlePlayCore}"
    const val playCoreKtx = "com.google.android.play:core-ktx:1.8.1"

    const val playServicesWearable =
        "com.google.android.gms:play-services-wearable:${Versions.playServicesWearable}"
    const val wearableSupport = "com.google.android.support:wearable:${Versions.wearableSupport}"
    const val wearableCompile = "com.google.android.wearable:wearable:${Versions.wearableSupport}"

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
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
        const val androidxTest = "1.3.1-alpha02"

        const val junit = "4.13"
        const val truth = "1.1"

        const val mockk = "1.10.3"

        const val robolectric = "4.4"
    }

    const val androidxArch = "androidx.arch.core:core-testing:${Versions.androidxArch}"
    const val junit = "junit:junit:${Versions.junit}"
    const val googleTruth = "com.google.truth:truth:${Versions.truth}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val androidxTestCore = "androidx.test:core-ktx:${Versions.androidxTest}"
    const val androidxTestExt = "androidx.test.ext:junit-ktx:1.1.2-rc03"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.0-M1"
}

object AndroidTestLibraries {
    object Versions {
        const val androidxArch = TestLibraries.Versions.androidxArch
        const val androidxEspresso = "3.1.0"
        const val androidxRoom = Libraries.Versions.androidxRoom
        const val androidxNavigation = Libraries.Versions.androidxNavigation
        const val androidxTest = TestLibraries.Versions.androidxTest
        const val androidxWork = Libraries.Versions.androidxWork

        const val truth = TestLibraries.Versions.truth
    }

    const val androidxArch = "androidx.arch.core:core-testing:${Versions.androidxArch}"
    const val androidxEspresso = "androidx.test.espresso:espresso-core:${Versions.androidxEspresso}"
    const val androidxRoom = "androidx.room:room-testing:${Versions.androidxRoom}"
    const val androidxNavigation =
        "androidx.navigation:navigation-testing:${Versions.androidxNavigation}"
    const val androidxTestRunner = "androidx.test:runner:${Versions.androidxTest}"
    const val androidxTestRules = "androidx.test:rules:${Versions.androidxTest}"
    const val androidxTestExt = "androidx.test.ext:junit-ktx:1.1.2-rc03"
    const val androidxTestCore = "androidx.test:core-ktx:${Versions.androidxTest}"
    const val androidxWork = "androidx.work:work-testing:${Versions.androidxWork}"

    const val googleTruth = "com.google.truth:truth:${Versions.truth}"
}
