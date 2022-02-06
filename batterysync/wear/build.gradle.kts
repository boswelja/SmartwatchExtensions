plugins {
    kotlin("multiplatform")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.boswelja.smartwatchextensions.library")
    kotlin("plugin.serialization") version "1.6.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                implementation(projects.devicemanagement.wear)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.watchconnection.wear)
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
                implementation(libs.androidx.datastore.proto)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.androidx.wear.complications.data.source)
                implementation(libs.bundles.compose.wear)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.androidx.wear.core)
            }
        }
        val androidAndroidTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.androidx.test.corektx)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.work.test)
                implementation(libs.koin.test)
                implementation(libs.mockk.android)
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
