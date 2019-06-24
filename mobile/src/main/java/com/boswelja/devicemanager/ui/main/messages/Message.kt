/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import com.boswelja.devicemanager.R

internal enum class Message(val iconRes: Int, val labelRes: Int, val shortLabelRes: Int, val descRes: Int = 0, val buttonLabelRes: Int = 0) {
    BatteryOptWarning(
            R.drawable.pref_ic_warning,
            R.string.message_battery_opt_warning_label,
            R.string.message_battery_opt_warning_label_short,
            R.string.message_battery_opt_warning_desc,
            R.string.message_battery_opt_warning_button_label),
    WatchChargeNotiWarning(
            R.drawable.pref_ic_warning,
            R.string.message_watch_charge_noti_warning_label,
            R.string.message_watch_charge_noti_warning_label_short,
            R.string.message_watch_charge_noti_warning_desc,
            R.string.message_watch_charge_noti_warning_button_label)
}
