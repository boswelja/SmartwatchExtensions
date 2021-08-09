package com.boswelja.smartwatchextensions.dashboard.ui

enum class DashboardDestination(
    val route: String
) {
    APP_MANAGER("app-manager"),
    BATTERY_SYNC_SETTINGS("battery-sync-settings"),
    DND_SYNC_SETTINGS("dnd-sync-settings"),
    PHONE_LOCKING_SETTINGS("phone-locking-settings"),
    PROXIMITY_SETTINGS("proximity-settings")
}
