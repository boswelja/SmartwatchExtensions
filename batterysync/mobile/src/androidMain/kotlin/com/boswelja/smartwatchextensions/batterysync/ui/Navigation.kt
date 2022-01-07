package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti.PhoneBatteryNotiSettingsScreen
import com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti.WatchBatteryNotiSettingsScreen

/**
 * Adds the Battery Sync navigation graph to the host Nav Graph.
 */
fun NavGraphBuilder.batterySyncNavigation(
    graphRoute: String,
    onNavigate: (route: String) -> Unit,
    screenModifier: Modifier = Modifier
) = navigation(
    BatterySyncDestinations.BATTERY_SYNC_SETTINGS.route,
    graphRoute
) {
    composable(BatterySyncDestinations.BATTERY_SYNC_SETTINGS.route) {
        BatterySyncSettingsScreen(
            modifier = screenModifier,
            onNavigate = onNavigate
        )
    }
    composable(BatterySyncDestinations.PHONE_BATTERY_NOTIFICATION_SETTINGS.route) {
        PhoneBatteryNotiSettingsScreen(screenModifier)
    }
    composable(BatterySyncDestinations.WATCH_BATTERY_NOTIFICATION_SETTINGS.route) {
        WatchBatteryNotiSettingsScreen(screenModifier)
    }
}

internal enum class BatterySyncDestinations(val route: String) {
    BATTERY_SYNC_SETTINGS("battery_sync_settings"),
    PHONE_BATTERY_NOTIFICATION_SETTINGS("phone_noti_settings"),
    WATCH_BATTERY_NOTIFICATION_SETTINGS("watch_noti_settings")
}
