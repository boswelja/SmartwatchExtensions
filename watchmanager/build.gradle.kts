plugins {
    kotlin("android")
    id("com.android.library")
    id("com.google.devtools.ksp") version "1.5.31-1.0.0"
    id("com.squareup.wire")
}

android {
    compileSdk = PackageInfo.targetSdk
    defaultConfig {
        minSdk = 23
        targetSdk = PackageInfo.targetSdk
        consumerProguardFile("proguard-rules.pro")

        ksp {
            arg("room.schemaLocation", "$rootDir/schemas")
        }
    }
}

dependencies {
    implementation(projects.common)
    implementation(libs.watchconnection.platform.wearos)

    implementation(libs.androidx.datastore.proto)

    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

wire {
    kotlin { }
}
