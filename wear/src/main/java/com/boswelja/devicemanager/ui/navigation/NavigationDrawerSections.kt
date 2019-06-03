/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.navigation

import com.boswelja.devicemanager.R

enum class NavigationDrawerSections(val titleRes: Int, val drawableRes: Int) {
    Main(R.string.navigation_controls_title, R.drawable.phone),
    Settings(R.string.navigation_settings_title, R.drawable.ic_settings);
}
