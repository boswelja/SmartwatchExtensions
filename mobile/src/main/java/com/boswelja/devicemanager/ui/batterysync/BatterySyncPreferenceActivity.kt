/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.batterysync

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.BatterySyncJob
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class BatterySyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): BasePreferenceFragment = BatterySyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = BatterySyncPreferenceWidgetFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)) {
            BatterySyncJob.startJob(watchConnectionManager)
        } else {
            BatterySyncJob.stopJob(watchConnectionManager)
        }
    }
}
