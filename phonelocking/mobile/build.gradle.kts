plugins {
    id("com.boswelja.smartwatchextensions.library")
    id("com.boswelja.smartwatchextensions.detekt")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.phonelocking.common)
                implementation(projects.settings.mobile)
                implementation(projects.devicemanagement.mobile)

                api(libs.watchconnection.mobile.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.bundles.compose.mobile)
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
