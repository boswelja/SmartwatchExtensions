plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.squareup.sqldelight")
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(projects.appmanager.common)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
                implementation(libs.watchconnection.mobile.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(projects.devicemanagement.mobile)
                implementation(libs.sqldelight.android)
                implementation(libs.androidx.work.ktx)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.bundles.lifecycle)
                implementation(libs.bundles.compose.mobile)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlitedriver)
                implementation(libs.mockk.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidAndroidTest by getting {
            dependencies {
                implementation(libs.androidx.test.corektx)
                implementation(libs.androidx.test.runner)
                implementation(libs.koin.test)
                implementation(libs.mockk.android)
                // Workaround for MockK 1.11.0 including a broken objenesis
                implementation("org.objenesis:objenesis:3.2")
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    testOptions {
        devices {
            create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel5api31") {
                device = "Pixel 5"
                apiLevel = 31
                systemImageSource = "aosp"
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

sqldelight {
    database("WatchAppDatabase") {
        packageName = "com.boswelja.smartwatchextensions.appmanager.database"
    }
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-38694
configurations {
    create("composeCompiler") {
        isCanBeConsumed = false
    }
}
dependencies {
    "composeCompiler"("androidx.compose.compiler:compiler:${libs.versions.composeCompiler.get()}")
}
android {
    afterEvaluate {
        val composeCompilerJar = configurations["composeCompiler"]
            .resolve()
            .singleOrNull()
            ?: error("Missing Compose compiler")
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.freeCompilerArgs += listOf("-Xuse-ir", "-Xplugin=$composeCompilerJar")
        }
    }
}
