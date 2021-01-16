@file:Suppress("SpellCheckingInspection")

const val kotlinVersion = "1.4.21"

object BuildPlugins {
    object Versions {
        const val buildToolsVersion = "7.0.0-alpha04"
        const val googleServicesVersion = "4.3.4"
        const val androidxNavigation = "2.3.2"
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
    const val kotlinKapt = "kotlin-kapt"
    const val safeArgs = "androidx.navigation.safeargs.kotlin"
}

object AndroidSdk {
    const val mobileMin = 23
    const val wearMin = 25
    const val compile = 30
    const val target = compile
}
