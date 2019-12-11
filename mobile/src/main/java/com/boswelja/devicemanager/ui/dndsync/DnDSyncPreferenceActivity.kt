/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.dndsync

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class DnDSyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): BasePreferenceFragment = DnDSyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sharedPreferences.getBoolean(PreferenceKey.DND_SYNC_TO_WATCH_KEY, false)) {
            Intent(this, DnDLocalChangeService::class.java).also {
                Compat.startForegroundService(this, it)
            }
        }
    }
}
