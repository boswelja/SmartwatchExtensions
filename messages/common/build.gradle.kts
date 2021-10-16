plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.squareup.wire")
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.wire.runtime)
            }
        }
        val androidMain by getting {
            dependencies { }
        }
    }
}

android {
    compileSdk = PackageInfo.targetSdk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
        targetSdk = PackageInfo.targetSdk
        consumerProguardFile("proguard-rules.pro")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

wire {
    sourcePath {
        srcDir("src/commonMain/proto")
    }
    kotlin { }
}