plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
    kotlin("plugin.serialization") version "1.6.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.phonelocking.common)
                implementation(projects.devicemanagement.wear)

                implementation(libs.watchconnection.wear)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.datastore.proto)
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.bundles.compose.wear)
                implementation(libs.androidx.wear.core)
                implementation(libs.androidx.wear.complications.data.source)
            }
        }
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
