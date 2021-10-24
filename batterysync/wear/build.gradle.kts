plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt").version("1.18.1")
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                api(libs.watchconnection.common)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.datastore.proto)
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
        consumerProguardFile("proguard-rules.pro")
    }
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    source = files("src")
    buildUponDefaultConfig = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
