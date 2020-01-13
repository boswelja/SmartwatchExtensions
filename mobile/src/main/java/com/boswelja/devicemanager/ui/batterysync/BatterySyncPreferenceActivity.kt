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
import com.boswelja.devicemanager.batterysync.database.Helper
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BatterySyncPreferenceActivity : BasePreferenceActivity() {

    private val coroutineScope = MainScope()

    var batteryStatsDatabase: WatchBatteryStatsDatabase? = null

    override fun createPreferenceFragment(): BasePreferenceFragment = BatterySyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = BatterySyncPreferenceWidgetFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coroutineScope.launch {
            batteryStatsDatabase = Helper.openDatabase(this@BatterySyncPreferenceActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryStatsDatabase?.close()
    }
}
