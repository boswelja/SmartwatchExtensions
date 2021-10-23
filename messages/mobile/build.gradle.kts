plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.squareup.sqldelight")
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.messages.common)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
            }
        }
    }
}

android {
    compileSdk = PackageInfo.targetSdk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
        targetSdk = PackageInfo.targetSdk
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

sqldelight {
    database("MessageDatabase") {
        packageName = "com.boswelja.smartwatchextensions.messages.database"
    }
}
