package com.boswelja.smartwatchextensions.appmanager

fun createAppList(count: Int): AppList {
    val appList = (0 until count).map {
        App(
            packageName = "com.my.app$it"
        )
    }
    return AppList(appList)
}
