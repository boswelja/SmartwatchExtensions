package com.boswelja.smartwatchextensions.core.devicemanagement

/**
 * Requests launching the app on the target device.
 */
const val LAUNCH_APP = "/launch_app"

/**
 * Request an app reset for the target device.
 */
const val RESET_APP = "/reset_app"

/**
 * Request the target device update it's capabilities.
 */
const val REQUEST_UPDATE_CAPABILITIES = "/update_capabilities"

/**
 * Request the source device registration status with the target device.
 */
const val CHECK_WATCH_REGISTERED_PATH = "/check_watch_registered"

/**
 * Notify the target device it is registered with the source device.
 */
const val WATCH_REGISTERED_PATH = "/watch_registered"

/**
 * Request the target device app version.
 */
const val REQUEST_APP_VERSION = "/request_app_version"
