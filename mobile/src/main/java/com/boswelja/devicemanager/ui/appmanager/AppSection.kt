package com.boswelja.devicemanager.ui.appmanager

import com.boswelja.devicemanager.common.appmanager.AppPackageInfo

class AppSection(public val sectionTitleRes: Int, public val appsInSection: ArrayList<AppPackageInfo>) {

    public fun countIncludingHeader(): Int =
            count() + 1

    public fun count(): Int = appsInSection.count()
}