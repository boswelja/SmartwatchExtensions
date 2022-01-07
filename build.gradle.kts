plugins {
    id("com.android.application") version "7.2.0-alpha06" apply false
    id("com.android.library") version "7.2.0-alpha06" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
    id("com.squareup.sqldelight") version libs.versions.sqldelight.get() apply false
    id("com.google.gms.google-services") version "4.3.10" apply false
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}
