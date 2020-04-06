/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import com.boswelja.devicemanager.common.appmanager.AppPackageInfo

internal class AppSection(val sectionTitleRes: Int, val appsInSection: ArrayList<AppPackageInfo>) {

    fun countIncludingHeader(): Int =
            count() + 1

    fun count(): Int = appsInSection.count()
}
