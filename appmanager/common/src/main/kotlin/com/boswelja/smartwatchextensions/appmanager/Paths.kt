package com.boswelja.smartwatchextensions.appmanager

/**
 * Request opening a packages launch activity. The package name should be sent alongside this.
 */
const val RequestOpenPackage = "/app_manager_request_open_package"

/**
 * Request uninstalling a package. The package name should be sent alongside this.
 */
const val RequestUninstallPackage = "/app_manager_request_uninstall_package"

/**
 * Notifies listeners of an incoming stream of [App] objects.
 */
const val NotifyAppSendingStart = "/app_manager_sending_all"

/**
 * Notifies listeners of the stream of [App] objects ending.
 */
const val NotifyAppSendingComplete = "/app_manager_sending_complete"

/**
 * Notifies the host that a list of apps were removed from the device.
 */
const val RemovedAppsList = "/appmanager_removed_apps"

/**
 * Notifies the host that a list of apps were added to the device
 */
const val AddedAppsList = "/appmanager_added_apps"

/**
 * Notifies the host that a list of apps were updated on the device
 */
const val UpdatedAppsList = "/appmanager_updated_apps"

/**
 * A message containing an app icon.
 */
const val RawAppIcon = "/appmanager_app_icon"

/**
 * Requests app cache validation.
 */
const val RequestValidateCache = "/appmanager_cache_check"

/**
 * A path for devices to declare they can manage installed apps
 */
const val ManageAppsCapability = "MANAGE_APPS"
