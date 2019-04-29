/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.job.JobParameters
import android.app.job.JobService
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.common.References

class BatteryUpdateJob : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        CommonUtils.updateBatteryStats(this, References.CAPABILITY_WATCH_APP)
        jobFinished(params, true)
        return false
    }

    companion object {
        const val BATTERY_PERCENT_JOB_ID = 5656299
    }
}