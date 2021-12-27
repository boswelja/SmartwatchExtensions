plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
}

kotlin {
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
