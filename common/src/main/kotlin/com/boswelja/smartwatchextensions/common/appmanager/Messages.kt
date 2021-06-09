package com.boswelja.smartwatchextensions.common.appmanager

object Messages {

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
     * An [App] object.
     */
    const val APP_DATA = "/app_manager_all_apps"

    /**
     * Requests cache validation. A hash code obtained from [CacheValidation.getHashCode] should be
     * sent alongside this.
     */
    const val VALIDATE_CACHE = "/app_manager_cache_check"
}
