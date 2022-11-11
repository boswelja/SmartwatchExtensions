import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.boswelja.smartwatchextensions"
    compileSdk = PackageInfo.targetSdk

    defaultConfig {
        minSdk = 28
        targetSdk = PackageInfo.targetSdk

        applicationId = PackageInfo.packageName
        versionCode = PackageInfo.getVersionCode('0')
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
        debug {
            applicationIdSuffix = DebugInfo.idSuffix
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            optimization {
                keepRules {
                    ignoreExternalDependencies("androidx.glance:glance-appwidget")
                }
            }
        }
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packagingOptions {
        resources.excludes.addAll(arrayOf("META-INF/AL2.0", "META-INF/LGPL2.1"))
    }
}

dependencies {
    implementation(projects.core.mobile)
    implementation(projects.appmanager.mobile)
    implementation(projects.batterysync.mobile)
    implementation(projects.dndsync.mobile)
    implementation(projects.onboarding.mobile)
    implementation(projects.phonelocking.mobile)
    implementation(projects.watchmanager.mobile)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.work)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.proto)

    implementation(libs.kotlinx.serialization.protobuf)

    implementation(libs.watchconnection.platform.wearos)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.workmanager)

    implementation(libs.sqldelight.android)

    implementation(libs.bundles.compose.mobile)

    testImplementation(libs.androidx.work.test)
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
