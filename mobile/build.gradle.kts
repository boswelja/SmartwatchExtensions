plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(PackageInfo.targetSdk)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(PackageInfo.targetSdk)

        applicationId = PackageInfo.packageName
        versionCode = PackageInfo.getVersionCode()
        versionName = PackageInfo.versionName

        testApplicationId = PackageInfo.packageName + ".test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("room.schemaLocation" to "$rootDir/schemas"))
            }
        }
    }

    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix(DebugInfo.idSuffix)
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
    }
}

dependencies {
    val androidxArch = "2.1.0"
    val androidxAppCompat = "1.3.0-alpha02"
    val androidxBrowser = "1.3.0"
    val androidxConstraintLayout = "2.1.0-alpha1"
    val androidxCoreKtx = "1.5.0-alpha05"
    val androidxEspresso = "3.1.0"
    val androidxFragment = "1.3.0-beta02"
    val androidxLifecycle = "2.3.0-beta01"
    val androidxNavigation = "2.3.2"
    val androidxPaging = "3.0.0-alpha10"
    val androidxPreference = "1.1.1"
    val androidxRoom = "2.3.0-alpha03"
    val androidxTest = "1.3.1-alpha02"
    val androidxTestExt = "1.1.2-rc03"
    val androidxWork = "2.5.0-beta02"

    val billingClient = "3.0.2"
    val googleMaterial = "1.3.0-alpha04"
    val googlePlayCore = "1.9.0"
    val playServicesWearable = "17.0.0"
    val timber = "4.7.1"
    val junit = "4.13"
    val truth = "1.1"
    val mockk = "1.10.5"
    val robolectric = "4.4"
    val coroutines = "1.4.2"

    implementation(project(":common"))
    implementation(kotlin("reflect"))

    implementation(platform("com.google.firebase:firebase-bom:26.2.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("androidx.appcompat:appcompat:$androidxAppCompat")
    implementation("androidx.browser:browser:$androidxBrowser")
    implementation("androidx.constraintlayout:constraintlayout:$androidxConstraintLayout")
    implementation("androidx.core:core-ktx:$androidxCoreKtx")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-common-java8:$androidxLifecycle")
    implementation("androidx.navigation:navigation-fragment-ktx:$androidxNavigation")
    implementation("androidx.navigation:navigation-ui-ktx:$androidxNavigation")
    implementation("androidx.paging:paging-runtime:$androidxPaging")
    implementation("androidx.preference:preference:$androidxPreference")
    implementation("androidx.room:room-runtime:$androidxRoom")
    implementation("androidx.room:room-ktx:$androidxRoom")
    implementation("androidx.work:work-runtime-ktx:$androidxWork")

    implementation("com.android.billingclient:billing-ktx:$billingClient")
    implementation("com.google.android.material:material:$googleMaterial")
    implementation("com.google.android.play:core:$googlePlayCore")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation("com.google.android.gms:play-services-wearable:$playServicesWearable")
    implementation("com.jakewharton.timber:timber:$timber")

    kapt("androidx.room:room-compiler:$androidxRoom")

    debugImplementation("androidx.fragment:fragment-testing:$androidxFragment")

    testImplementation("androidx.arch.core:core-testing:$androidxArch")
    testImplementation("androidx.test:core-ktx:$androidxTest")
    testImplementation("androidx.test.ext:junit-ktx:$androidxTestExt")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines")
    testImplementation("junit:junit:$junit")
    testImplementation("com.google.truth:truth:$truth")
    testImplementation("io.mockk:mockk:$mockk")
    testImplementation("org.robolectric:robolectric:$robolectric")

    androidTestImplementation("androidx.test.espresso:espresso-core:$androidxEspresso")
    androidTestImplementation("androidx.navigation:navigation-testing:$androidxNavigation")
    androidTestImplementation("androidx.test:runner:$androidxTest")
    androidTestImplementation("androidx.test:rules:$androidxTest")
    androidTestImplementation("androidx.test:core-ktx:$androidxTest")
    androidTestImplementation("com.google.truth:truth:$truth")
    androidTestImplementation("io.mockk:mockk-android:$mockk")
}
