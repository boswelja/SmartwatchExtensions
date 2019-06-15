/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.interruptfiltersync.BaseInterruptFilterRemoteChangeReceiver

class InterruptFilterRemoteChangeReceiver : BaseInterruptFilterRemoteChangeReceiver() {

    override fun isReceiving(): Boolean =
            sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false) ||
                    sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false)

    override fun setInterruptionFilter(interruptFilterEnabled: Boolean) {
        Utils.setInterruptionFilter(this, interruptFilterEnabled)
    }
}
