plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(projects.appmanager.common)
                implementation(libs.watchconnection.wear)
                implementation(libs.koin.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }
        val androidAndroidTest by getting {
            dependencies {
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
