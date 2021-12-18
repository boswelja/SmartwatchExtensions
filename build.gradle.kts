plugins {
    id("com.android.application") version "7.2.0-alpha06" apply false
    id("com.android.library") version "7.2.0-alpha06" apply false
    id("org.jetbrains.kotlin.android") version "1.5.31" apply false
    id("com.squareup.sqldelight") version libs.versions.sqldelight.get() apply false
    id("com.squareup.wire") version libs.versions.wire.get() apply false
    id("com.google.gms.google-services") version "4.3.10" apply false
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    repositories {
        google()
        mavenCentral()
    }
    detekt {
        config = files("$rootDir/config/detekt/detekt.yml")
        source = files("src")
        buildUponDefaultConfig = true
        parallel = true
    }
}
