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
                api(libs.watchconnection.common)
                api(libs.wire.runtime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies { }
        }
        val androidTest by getting { }
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

wire {
    sourcePath {
        srcDir("src/commonMain/proto")
    }
    kotlin { }
}
