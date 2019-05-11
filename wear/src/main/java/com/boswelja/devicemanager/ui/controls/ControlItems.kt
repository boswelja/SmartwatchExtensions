/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.controls

import com.boswelja.devicemanager.R

enum class ControlItems(val titleRes: Int, val drawableRes: Int) {
    LockPhone(R.string.lock_phone_label, R.drawable.ic_phone_lock),
    PhoneBattery(0, R.drawable.ic_phone_battery)
}
