package com.boswelja.smartwatchextensions.appmanager

import kotlinx.coroutines.runBlocking

actual fun <T> runSuspendingTest(block: suspend () -> T) {
    runBlocking { block() }
}

fun createAppList(count: Int): AppList {
    val appList = (0 until count).map {
        App(
            packageName = "com.my.app$it"
        )
    }
    return AppList(appList)
}
