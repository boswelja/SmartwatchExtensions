package com.boswelja.devicemanager.tasks

import android.app.job.JobParameters
import android.app.job.JobService
import com.boswelja.devicemanager.common.Utils

class BatteryInfoUpdate: JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Utils.updateBatteryStats(this)
        jobFinished(params, true)
        return false
    }
}