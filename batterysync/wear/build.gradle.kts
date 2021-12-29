plugins {
    kotlin("multiplatform")
    id("com.boswelja.smartwatchextensions.detekt")
    id("com.boswelja.smartwatchextensions.library")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.batterysync.common)
                implementation(libs.watchconnection.wear)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.datastore.proto)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.androidx.wear.complications.data.source)
                implementation(libs.bundles.compose.wear)
            }
        }
    }
}

android {
    buildFeatures.compose = true
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
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
