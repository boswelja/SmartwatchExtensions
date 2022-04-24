package com.boswelja.smartwatchextensions.batterysync

/**
 * A path for a message containing battery stats.
 */
const val BatteryStatus = "/battery_status"

/**
 * A path for a message requesting a battery stats update.
 */
const val RequestBatteryStatus = "/request_battery_update"

/**
 * A path for devices to declare they can sync battery
 */
const val SyncBatteryStatus = "SYNC_BATTERY"
