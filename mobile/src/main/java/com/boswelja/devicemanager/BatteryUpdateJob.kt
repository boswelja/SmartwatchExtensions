/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.job.JobParameters
import android.app.job.JobService
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.BatterySyncUtils.updateBatteryStats

class BatteryUpdateJob : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        updateBatteryStats(this, References.CAPABILITY_WATCH_APP)
        jobFinished(params, true)
        return false
    }

    companion object {
        const val BATTERY_PERCENT_JOB_ID = 5656299
    }
}
