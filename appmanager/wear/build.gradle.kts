plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(projects.appmanager.common)
                implementation(libs.watchconnection.wear)
                implementation(libs.koin.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
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
