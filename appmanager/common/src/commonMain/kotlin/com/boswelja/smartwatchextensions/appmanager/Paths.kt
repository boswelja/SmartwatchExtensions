package com.boswelja.smartwatchextensions.appmanager

/**
 * Request opening a packages launch activity. The package name should be sent alongside this.
 */
const val REQUEST_OPEN_PACKAGE = "/app_manager_request_open_package"

/**
 * Request uninstalling a package. The package name should be sent alongside this.
 */
const val REQUEST_UNINSTALL_PACKAGE = "/app_manager_request_uninstall_package"

/**
 * Notifies listeners of an incoming stream of [App] objects.
 */
const val APP_SENDING_START = "/app_manager_sending_all"

/**
 * Notifies listeners of the stream of [App] objects ending.
 */
const val APP_SENDING_COMPLETE = "/app_manager_sending_complete"

/**
 * Notifies the host that a list of apps were removed from the device.
 */
const val REMOVED_APPS = "/appmanager_removed_apps"

/**
 * Notifies the host that a list of apps were added to the device
 */
const val ADDED_APPS = "/appmanager_added_apps"

/**
 * Notifies the host that a list of apps were updated on the device
 */
const val UPDATED_APPS = "/appmanager_updated_apps"

/**
 * A message containing an app icon.
 */
const val APP_ICON = "/appmanager_app_icon"

/**
 * Requests app cache validation.
 */
const val VALIDATE_CACHE = "/appmanager_cache_check"
