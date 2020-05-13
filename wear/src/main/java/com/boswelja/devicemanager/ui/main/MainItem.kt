/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class MainItem(
    val itemId: Int,
    @StringRes
    val textRes: Int,
    @DrawableRes
    val iconRes: Int,
    val enabled: Boolean = true,
    val extra: Int = -1
)
