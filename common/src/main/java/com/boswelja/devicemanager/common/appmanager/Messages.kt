package com.boswelja.devicemanager.common.appmanager

object Messages {

    const val PACKAGE_ADDED = "/app_manager_package_added"
    const val PACKAGE_REMOVED = "/app_manager_package_removed"
    const val PACKAGE_UPDATED = "/app_manager_package_updated"

    const val REQUEST_OPEN_PACKAGE = "/app_manager_request_open_package"
    const val REQUEST_UNINSTALL_PACKAGE = "/app_manager_request_uninstall_package"

    const val START_SERVICE = "/start_app_manager_service"
    const val STOP_SERVICE = "/stop_app_manager_service"

    const val SERVICE_RUNNING = "/app_manager_running"

    const val EXPECTED_APP_COUNT = "/app_manager_expected_count"
}
