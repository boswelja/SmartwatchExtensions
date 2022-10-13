package com.boswelja.smartwatchextensions.core.devicemanagement

/**
 * Requests launching the app on the target device.
 */
const val RequestLaunchApp = "/launch_app"

/**
 * Request an app reset for the target device.
 */
const val RequestResetApp = "/reset_app"

/**
 * Request the target device update it's capabilities.
 */
const val RequestUpdateCapabilities = "/update_capabilities"

/**
 * Request the source device registration status with the target device.
 */
const val CheckWatchRegistered = "/check_watch_registered"

/**
 * Notify the target device it is registered with the source device.
 */
const val ConfirmWatchRegistered = "/watch_registered"
