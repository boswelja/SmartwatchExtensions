/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.batterysync

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class BatterySyncPreferenceActivity : BasePreferenceActivity() {

    var batteryStatsDatabase: WatchBatteryStatsDatabase? = null
    var batteryStatsDatabaseEventInterface: BatteryStatsDatabaseEventInterface? = null

    override fun createPreferenceFragment(): BasePreferenceFragment = BatterySyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = BatterySyncPreferenceWidgetFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")
        openBatteryStatsDatabase()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy() called")
        batteryStatsDatabase?.close()
    }

    /**
     * Opens a [WatchBatteryStatsDatabase] instance and
     * calls [BatteryStatsDatabaseEventInterface.onOpened] if possible.
     */
    private fun openBatteryStatsDatabase() {
        Timber.d("openBatteryStatsDatabase() called")
        coroutineScope.launch(Dispatchers.IO) {
            batteryStatsDatabase = WatchBatteryStatsDatabase.open(this@BatterySyncPreferenceActivity)
            if (batteryStatsDatabaseEventInterface != null) {
                batteryStatsDatabaseEventInterface!!.onOpened()
            } else {
                Timber.i("batteryStatsDatabaseEventInterface null, ignoring")
            }
        }
    }

    interface BatteryStatsDatabaseEventInterface {
        fun onOpened()
    }
}
