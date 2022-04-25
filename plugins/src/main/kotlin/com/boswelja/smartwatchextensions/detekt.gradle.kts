package com.boswelja.smartwatchextensions

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    parallel = true
}
