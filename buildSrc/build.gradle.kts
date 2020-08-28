repositories {
    jcenter()
}

plugins {
    `kotlin-dsl`
}

// Workaround for AGP 4.2.0-alpha08 bug
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.0")
}