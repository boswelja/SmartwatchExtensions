package com.boswelja.devicemanager.receiver

import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.prefsynclayer.BasePreferenceChangeReceiver

class RemotePreferenceChangeReceiver : BasePreferenceChangeReceiver() {

    override fun handleStartServices(dndSyncWithTheater: Boolean, batterySyncEnabled: Boolean) {
        if (batterySyncEnabled) {
            Utils.createBatterySyncJob(this)
        } else {
            Utils.stopBatterySyncJob(this)
        }
    }

}