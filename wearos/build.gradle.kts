plugins {
    id("com.android.application")
    kotlin("android")
    id("com.squareup.wire")
}

android {
    compileSdk = PackageInfo.targetSdk

    defaultConfig {
        minSdk = 26
        targetSdk = PackageInfo.targetSdk

        applicationId = PackageInfo.packageName
        // Add 1 here to ensure it's different from mobile module
        versionCode = PackageInfo.getVersionCode('1')
        versionName = PackageInfo.getVersionName()

        testApplicationId = PackageInfo.packageName + ".test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(projects.appmanager.wear)
    implementation(projects.batterysync.wear)
    implementation(projects.dndsync)
    implementation(projects.phonelocking)
    implementation(projects.settings)
    implementation(projects.versionsync)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.androidx.wear.core)
    implementation(libs.androidx.wear.complications.data.source)
    implementation(libs.androidx.work.ktx)

    implementation(libs.timber)
    implementation(libs.watchconnection.wear)

    implementation(libs.bundles.compose.wear)

    testImplementation(libs.androidx.test.corektx)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
    testImplementation(libs.strikt.mockk)
    testImplementation(libs.mockk.core)
    testImplementation(libs.robolectric)

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
