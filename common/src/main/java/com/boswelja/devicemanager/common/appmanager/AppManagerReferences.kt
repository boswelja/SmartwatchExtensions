/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.appmanager

object AppManagerReferences {

    const val GET_ALL_PACKAGES = "/app_manager_request_all_packages"

    const val PACKAGE_ADDED = "/app_manager_package_added"
    const val PACKAGE_REMOVED = "/app_manager_package_removed"

    const val REQUEST_UNINSTALL_PACKAGE = "/app_manager_request_uninstall_package"

    const val START_SERVICE = "/start_app_manager_service"
    const val STOP_SERVICE = "/stop_app_manager_service"
}
