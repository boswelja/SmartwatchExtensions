plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("com.android.tools.build:gradle:7.2.0-alpha06")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
}
