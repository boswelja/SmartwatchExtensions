/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import com.boswelja.devicemanager.R

object MainItems {

    const val BATTERY_SYNC_ITEM_ID = 0
    const val PHONE_LOCKING_ITEM_ID = 1

    const val SETTINGS_ITEM_ID = 2

    val EXTENSIONS = arrayListOf(
            MainItem(BATTERY_SYNC_ITEM_ID,
                    R.string.battery_sync_disabled,
                    R.drawable.ic_phone_battery),
            MainItem(PHONE_LOCKING_ITEM_ID,
                    R.string.lock_phone_disabled_message,
                    R.drawable.ic_phone_lock)
    )

    val APP = arrayListOf(
            MainItem(SETTINGS_ITEM_ID,
                    R.string.navigation_settings_title,
                    R.drawable.ic_settings)
    )
}
