/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.dndsync.BaseDnDLocalChangeListener

@RequiresApi(Build.VERSION_CODES.M)
class DnDLocalChangeListener : BaseDnDLocalChangeListener() {

    override val interruptFilterSendEnabledKey: String = PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY
}
