plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "1.4.31-1.0.0-alpha06"
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

        ksp {
            arg("room.schemaLocation", "$rootDir/schemas")
        }
    }

    buildFeatures.viewBinding = true
    buildFeatures.compose = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-beta02"
    }

    packagingOptions {
        resources.excludes.addAll(arrayOf("META-INF/AL2.0", "META-INF/LGPL2.1"))
    }
}

dependencies {
    val androidxArch = "2.1.0"
    val androidxAppCompat = "1.3.0-beta01"
    val androidxBrowser = "1.3.0"
    val androidxCoreKtx = "1.5.0-beta02"
    val androidxEspresso = "3.1.0"
    val androidxFragment = "1.3.0"
    val androidxLifecycle = "2.3.0"
    val androidxPreference = "1.1.1"
    val androidxRoom = "2.3.0-beta03"
    val androidxTest = "1.4.0-alpha05"
    val androidxTestExt = "1.1.3-alpha04"
    val androidxWork = "2.7.0-alpha02"

    val compose = "1.0.0-beta02"
    val lifecycleCompose = "1.0.0-alpha02"
    val activityCompose = "1.3.0-alpha03"
    val navigationCompose = "1.0.0-alpha09"

    val billingClient = "3.0.2"
    val googlePlayCore = "1.9.1"
    val playServicesWearable = "17.0.0"
    val timber = "4.7.1"
    val junit = "4.13.2"
    val truth = "1.1.2"
    val mockk = "1.11.0"
    val robolectric = "4.5.1"
    val coroutines = "1.4.3"

    implementation(project(":common"))

    implementation(platform("com.google.firebase:firebase-bom:26.2.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("androidx.appcompat:appcompat:$androidxAppCompat")
    implementation("androidx.browser:browser:$androidxBrowser")
    implementation("androidx.core:core-ktx:$androidxCoreKtx")
    implementation("androidx.fragment:fragment-ktx:$androidxFragment")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-common-java8:$androidxLifecycle")
    implementation("androidx.lifecycle:lifecycle-service:$androidxLifecycle")
    implementation("androidx.preference:preference:$androidxPreference")
    implementation("androidx.room:room-runtime:$androidxRoom")
    implementation("androidx.room:room-ktx:$androidxRoom")
    implementation("androidx.work:work-runtime-ktx:$androidxWork")

    implementation("com.android.billingclient:billing-ktx:$billingClient")
    implementation("com.google.android.play:core:$googlePlayCore")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation("com.google.android.gms:play-services-wearable:$playServicesWearable")
    implementation("com.jakewharton.timber:timber:$timber")

    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-tooling:$compose")
    implementation("androidx.compose.ui:ui-viewbinding:$compose")
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.material:material:$compose")
    implementation("androidx.compose.material:material-icons-core:$compose")
    implementation("androidx.compose.material:material-icons-extended:$compose")
    implementation("androidx.compose.runtime:runtime-livedata:$compose")
    implementation("androidx.activity:activity-compose:$activityCompose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleCompose")
    implementation("androidx.navigation:navigation-compose:$navigationCompose")

    ksp("androidx.room:room-compiler:$androidxRoom")

    testImplementation("androidx.arch.core:core-testing:$androidxArch")
    testImplementation("androidx.test:core-ktx:$androidxTest")
    testImplementation("androidx.test.ext:junit-ktx:$androidxTestExt")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines")
    testImplementation("junit:junit:$junit")
    testImplementation("com.google.truth:truth:$truth")
    testImplementation("io.mockk:mockk:$mockk")
    testImplementation("org.robolectric:robolectric:$robolectric")

    androidTestImplementation("androidx.arch.core:core-testing:$androidxArch")
    androidTestImplementation("androidx.test.espresso:espresso-core:$androidxEspresso")
    androidTestImplementation("androidx.test:runner:$androidxTest")
    androidTestImplementation("androidx.test:rules:$androidxTest")
    androidTestImplementation("androidx.test:core-ktx:$androidxTest")
    androidTestImplementation("com.google.truth:truth:$truth")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose")
    androidTestImplementation("io.mockk:mockk-android:$mockk")
}
