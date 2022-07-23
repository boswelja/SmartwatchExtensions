plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "1.6.20"
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions"
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
    implementation(projects.appmanager.wear)
    implementation(projects.batterysync.wear)
    implementation(projects.core.wear)
    implementation(projects.dndsync.wear)
    implementation(projects.phonelocking.wear)
    implementation(projects.proximity.wear)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.datastore.proto)
    implementation(libs.androidx.wear.core)
    implementation(libs.androidx.wear.complications.data.source)
    implementation(libs.androidx.work)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.protobuf)

    implementation(libs.watchconnection.wear)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.bundles.compose.wear)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk.core)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.mockk.android)
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}
