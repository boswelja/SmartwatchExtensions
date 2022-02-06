plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.squareup.sqldelight")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.watchconnection.common)
                api(projects.settings.common)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin.core)
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
                implementation(libs.sqldelight.android)
                implementation(libs.bundles.compose.mobile)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlitedriver)
            }
        }
    }
}

android {
    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

sqldelight {
    database("WatchSettingsDatabase") {
        packageName = "com.boswelja.smartwatchextensions.settings.database"
    }
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-38694
configurations {
    create("composeCompiler") {
        isCanBeConsumed = false
    }
}
dependencies {
    "composeCompiler"("androidx.compose.compiler:compiler:${libs.versions.compose.get()}")
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
