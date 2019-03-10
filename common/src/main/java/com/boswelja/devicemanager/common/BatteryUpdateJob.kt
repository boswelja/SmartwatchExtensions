/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.app.job.JobParameters
import android.app.job.JobService

class BatteryUpdateJob : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        CommonUtils.updateBatteryStats(this)
        jobFinished(params, true)
        return false
    }

    companion object {
        const val BATTERY_PERCENT_JOB_ID = 5656299
    }
}