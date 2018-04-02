/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.tasks

import android.app.job.JobParameters
import android.app.job.JobService
import com.boswelja.devicemanager.common.Utils

class BatteryInfoUpdate : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Utils.updateBatteryStats(this)
        jobFinished(params, true)
        return false
    }
}