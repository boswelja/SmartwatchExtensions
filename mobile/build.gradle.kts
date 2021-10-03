import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.5.31-1.0.0"
    id("com.google.gms.google-services")
    id("com.squareup.wire")
}

android {
    compileSdk = PackageInfo.targetSdk

    defaultConfig {
        minSdk = 28
        targetSdk = PackageInfo.targetSdk

        applicationId = PackageInfo.packageName
        versionCode = PackageInfo.getVersionCode('0')
        versionName = PackageInfo.getVersionName()

        testApplicationId = PackageInfo.packageName + ".test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$rootDir/schemas")
        }
    }

    buildFeatures.compose = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = DebugInfo.idSuffix
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    packagingOptions {
        resources.excludes.addAll(arrayOf("META-INF/AL2.0", "META-INF/LGPL2.1"))
    }
}

dependencies {
    implementation(projects.common)
    implementation(projects.batterysync)

    implementation(libs.firebase.analytics)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.room)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.proto)

    implementation(libs.billingclient)
    implementation(libs.googleplay.core)
    implementation(libs.googleplay.corektx)
    implementation(libs.migration)
    implementation(libs.timber)
    implementation(libs.watchconnection.platform.wearos)

    implementation(libs.bundles.compose.mobile)
    implementation(libs.androidx.compose.ui.viewbinding)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.androidx.work.test)
    testImplementation(libs.androidx.arch.core.test)
    testImplementation(libs.androidx.test.corektx)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
    testImplementation(libs.strikt.mockk)
    testImplementation(libs.mockk.core)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.arch.core.test)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.corektx)
    androidTestImplementation(libs.strikt.core)
    androidTestImplementation(libs.strikt.mockk)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.mockk.android)
}

wire {
    kotlin {
        android = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
